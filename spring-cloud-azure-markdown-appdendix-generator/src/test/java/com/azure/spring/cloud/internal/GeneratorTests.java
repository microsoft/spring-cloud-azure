/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.spring.cloud.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.assertj.core.api.BDDAssertions.then;

class GeneratorTests {

	private static final String DATE = "MM/dd/yyyy";

	private static final String INCLUSION_PATTERN = "spring.cloud.azure.*";

	@Test
	void notCreateFileNoPropertiesFound() throws URISyntaxException {
		Main.Generator generator = new Main.Generator() {
			@Override
			protected List<Resource> getSpringConfigurationMetadataJsonFilesInClasspath() {
				return new ArrayList<>();
			}
		};
		File file = getOutputFilePath();
		String outputFile = file.toString().replaceAll("\\\\", "/");
		generator.generate(outputFile, INCLUSION_PATTERN, DATE);
		then(file).doesNotExist();
	}

	@Test
	void CreateFilePropertiesFound() throws URISyntaxException {
		Main.Generator generator = new Main.Generator() {
			@Override
			protected List<Resource> getSpringConfigurationMetadataJsonFilesInClasspath() throws IOException {
				Resource[] resources = new PathMatchingResourcePatternResolver()
						.getResources("/with-azure-in-name.json");
				return Arrays.stream(resources).filter(resource -> {
					try {
						return resource.getURL().toString().contains("azure");
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					return false;
				}).collect(Collectors.toList());
			}
		};
		File file = getOutputFilePath();
		String outputFile = file.toString().replaceAll("\\\\", "/");
		generator.generate(outputFile, INCLUSION_PATTERN, DATE);
		then(file).exists();
		assert compareFile(outputFile) : "Files are different!";
	}

	private Boolean compareFile(String file2) {
		boolean result = true;
		try {
			BufferedInputStream inFile1 = new BufferedInputStream(
					new FileInputStream("src/test/resources/configuration-properties-output.md"));
			BufferedInputStream inFile2 = new BufferedInputStream(new FileInputStream(file2));
			if (inFile1.available() == inFile2.available()) {
				while (inFile1.read() != -1 && inFile2.read() != -1) {
					if (inFile1.read() != inFile2.read()) {
						result = false;
					}
				}
			}
			else {
				result = false;
			}
			inFile1.close();
			inFile2.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private File getOutputFilePath() throws URISyntaxException {
		URL root = GeneratorTests.class.getResource(".");
		assert root != null;
		return new File(root.toURI().toString().substring(6), "configuration-properties-output.md");
	}

}
