/*
 * Copyright 2012-2020 the original author or authors.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

/**
 * Generate Markdown files.
 */
public final class Main {

	private Main() {
	}

	public static void main(String... args) {
		String outputFile = args[0].replace("adoc", "md").replace("_", "");
		String inclusionPattern = args.length > 1 ? args[1] : ".*";
		File parent = new File(outputFile).getParentFile();
		if (!parent.exists()) {
			System.out.println(
					"No parent directory [" + parent + "] found. Will not generate the configuration properties file");
			return;
		}
		new Generator().generate(outputFile, inclusionPattern, getCurrentDate());
	}

	static String getCurrentDate() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		simpleDateFormat.applyPattern("MM/dd/yyyy");
		Date date = new Date();
		return simpleDateFormat.format(date);
	}

	/**
	 * Generate Markdown files based on prefix and filename.
	 */
	static class Generator {

		/**
		 * Generate files prefixed with _configuration-properties.
		 * @param outputFile the output file
		 * @param inclusionPattern the inclusion pattern
		 */
		void generate(String outputFile, String inclusionPattern, String date) {
			try {
				System.out.println("Parsing all configuration metadata");
				List<Resource> resources = getSpringConfigurationMetadataJsonFilesInClasspath();
				Map<String, Object> descriptions = new HashMap<>();
				int count = 0;
				int propertyCount = 0;
				Pattern pattern = Pattern.compile(inclusionPattern);
				ObjectMapper objectMapper = new ObjectMapper();
				for (Resource resource : resources) {
					byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
					Map<String, Object> rootMap = objectMapper.readValue(bytes, HashMap.class);
					List<Map<String, Object>> properties = (List<Map<String, Object>>) rootMap.get("properties");
					count++;
					propertyCount += properties.size();
					properties.forEach(propertyItem -> {
						String name = String.valueOf(propertyItem.get("name"));
						if (!pattern.matcher(name).matches()) {
							return;
						}
						Object description = propertyItem.get("description");
						Object defaultValue = propertyItem.get("defaultValue");
						descriptions.put(name, defaultValue == null ? description
								: description + " The default value is " + "`" + defaultValue + "`" + ".");
					});
				}
				System.out.println(
						"Found [" + count + "] Azure projects configuration metadata jsons. [" + descriptions.size()
								+ "/" + propertyCount + "] were matching the pattern [" + inclusionPattern + "]");
				System.out.println("Successfully built the description table");
				if (descriptions.isEmpty()) {
					System.out.println("Will not update the table, since no configuration properties were found!");
					return;
				}
				generatePropertiesFile(outputFile, descriptions, date);
			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		/**
		 * Get all "spring-configuration-metadata.json" files in classpath.
		 */
		protected List<Resource> getSpringConfigurationMetadataJsonFilesInClasspath() throws IOException {
			Resource[] resources = new PathMatchingResourcePatternResolver()
					.getResources("classpath*:/META-INF/spring-configuration-metadata.json");
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

		private String paddingWithChar(String string, char c, int resultLength) {
			if ('-' == c) {
				return String.format("%1$-" + resultLength + "s", string.trim()).replace(' ', c);
			}
			else {
				return String.format("%1$-" + resultLength + "s", string.trim());
			}
		}

		private String capitalize(String letters) {
			if ("db".equals(letters) | "jms".equals(letters) | "b2c".equals(letters)) {
				return letters.toUpperCase();
			}
			else {
				return letters.substring(0, 1).toUpperCase().concat(letters.substring(1));
			}
		}

		private String generateAnchorName(String outputFile) {
			outputFile = outputFile.substring(outputFile.lastIndexOf("/") + "_configuration-properties-".length(),
					outputFile.lastIndexOf(".")).replace('-', ' ').trim();
			if ("all".equals(outputFile)) {
				return "List of configuration";
			}
			String[] value = outputFile.split(" ");
			StringBuilder result = new StringBuilder(" ");
			for (String s : value) {
				result.append(capitalize(s)).append(" ");
			}
			return result.toString().trim();
		}

		private void generatePropertiesFile(String outputFile, Map<String, Object> descriptions, String date) {
			int nameColumnWidth = Objects
					.requireNonNull(
							descriptions.keySet().stream().max(Comparator.comparingInt(String::length)).orElse(null))
					.trim().length();
			int descriptionColumnWidth = descriptions.values().stream().map(Object::toString)
					.max(Comparator.comparingInt(String::length)).orElse(null).trim().length();
			Path path = Paths.get(outputFile);
			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				writer.write("---");
				writer.newLine();
				writer.write("ms.author: v-yonghuiye");
				writer.newLine();
				writer.write("ms.date: " + date);
				writer.newLine();
				writer.write("---");
				writer.newLine();
				writer.newLine();
				writer.write("## " + generateAnchorName(outputFile) + " properties");
				writer.newLine();
				writer.newLine();
				writer.write("> [!div class=\"mx-tdBreakAll\"]");
				writer.newLine();
				String propertyHeader = paddingWithChar("Property", ' ', nameColumnWidth + 1);
				String descriptionHeader = paddingWithChar("Description", ' ', descriptionColumnWidth + 1);
				writer.write("> | " + propertyHeader + "| " + descriptionHeader + "|");
				writer.newLine();
				String markdownTableCol1 = paddingWithChar("", '-', nameColumnWidth + 2);
				String markdownTableCol2 = paddingWithChar("", '-', descriptionColumnWidth + 2);
				writer.write("> |" + markdownTableCol1 + "|" + markdownTableCol2 + "|");
				writer.newLine();
				for (Map.Entry<String, Object> description : descriptions.entrySet()) {
					String nameCol = paddingWithChar(description.getKey(), ' ', nameColumnWidth);
					String descriptionCol = paddingWithChar(description.getValue().toString(), ' ',
							descriptionColumnWidth);
					writer.write("> | " + nameCol + " | " + descriptionCol + " |");
					writer.newLine();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Successfully stored the output Markdown file!");
		}

	}

}
