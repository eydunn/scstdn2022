/*
 * Copyright 2022 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.creekfunctions;

import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.messaging.Message;

public class LogCreekMeasurements implements Consumer<Message<String>> {

	ObjectMapper objectMapper;

	public LogCreekMeasurements() {
		System.out.println("***** Constructor *****");
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
	}

	@Override
	public void accept(Message<String> stringMessage) {
		List<CreekMeasurement> creekMeasurements = null;
		System.out.println("***** HERE *****");
		try {
			creekMeasurements = objectMapper.readValue(stringMessage.getPayload(),
					new TypeReference<List<CreekMeasurement>>() {
					});
		}
		catch (JsonProcessingException jpe) {
			throw new IllegalStateException("Unable to parse CreekMeasurements", jpe);
		}
		CreekMeasurement controlMeasurement = null;
		CreekMeasurement previousMeasurement = null;
		for (CreekMeasurement measurement : creekMeasurements) {
			if (controlMeasurement == null) {
				controlMeasurement = measurement;
				continue;
			}
			if (!measurement.getSensorId().equals(controlMeasurement.getSensorId())) {

				System.out.println(previousMeasurement.getSensorId() + " "
						+ getSymbol(controlMeasurement, previousMeasurement));

				controlMeasurement = measurement;
			}
			previousMeasurement = measurement;
		}
		System.out.println(previousMeasurement.getSensorId() + " "
				+ getSymbol(controlMeasurement, previousMeasurement));

	}

	private String getSymbol(CreekMeasurement controlMeasurement, CreekMeasurement previousMeasurement) {
		double warnPercentage = ((previousMeasurement.getStreamHeight() - controlMeasurement.getStreamHeight() )
				/ previousMeasurement.getStreamHeight());
		System.out.println("*******" + warnPercentage);
		String symbol = Character.toString('\u2705');
		if (warnPercentage > .05) {
			symbol = Character.toString('\u274c');
		}
		return symbol;
	}
}