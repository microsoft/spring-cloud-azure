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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.assertj.core.api.BDDAssertions.then;

class GeneratorTests {

	private static final String DATE = "MM/dd/yyyy";

	private static final String INCLUSION_PATTERN = "spring.cloud.azure.*";

	@BeforeEach
	public void before() {
		File file = getOutputFilePath();
		if (file.delete()) {
			System.out.println("File deleted!");
		}
	}

	@Test
	void notCreateFileWhenNoPropertiesFound() {
		Main.Generator generator = new Main.Generator() {
			@Override
			protected List<Resource> getSpringConfigurationMetadataJsonFilesInClasspath() {
				return new ArrayList<>();
			}
		};
		File file = getOutputFilePath();
		String outputFile = file.getPath();
		generator.generate(outputFile, INCLUSION_PATTERN, DATE);
		then(file).doesNotExist();
	}

	@Test
	void createFileWhenPropertiesFound() throws IOException {
		Main.Generator generator = new Main.Generator() {
			@Override
			protected List<Resource> getSpringConfigurationMetadataJsonFilesInClasspath() throws IOException {
				Resource[] resources = new PathMatchingResourcePatternResolver()
						.getResources("/with-azure-in-name.json");
				return Arrays.asList(resources);
			}
		};
		File file = getOutputFilePath();
		String outputFile = file.getPath();
		generator.generate(outputFile, INCLUSION_PATTERN, DATE);
		then(file).exists();
		compareFile(outputFile);
	}

	private void compareFile(String file2) throws IOException {
		try (BufferedInputStream inFile1 = new BufferedInputStream(
				new FileInputStream("src/test/resources/configuration-properties-output.md"));
				BufferedInputStream inFile2 = new BufferedInputStream(new FileInputStream(file2))) {
			Assertions.assertThat(inFile1).hasSameContentAs(inFile2);
		}
	}

	private File getOutputFilePath() {
		URL root = this.getClass().getResource(".");
		if (root == null) {
			throw new IllegalArgumentException("The output file path is not found!");
		}
		else {
			return new File(root.getPath(), "configuration-properties-output.md");
		}
	}

}
