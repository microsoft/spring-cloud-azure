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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

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

		String outputFile = args[0];
		outputFile = outputFile.replace("adoc", "md").replace("_", "");
		String inclusionPattern = args.length > 1 ? args[1] : ".*";
		File parent = new File(outputFile).getParentFile();
		if (!parent.exists()) {
			System.out.println(
					"No parent directory [" + parent + "] found. Will not generate the configuration properties file");
			return;
		}
		new Generator().generate(outputFile, inclusionPattern);
	}

	/**
	 * Generate Markdown files based on prefix and filename.
	 */
	static class Generator {

		void generate(String outputFile, String inclusionPattern) {
			try {
				System.out.println("Parsing all configuration metadata");
				Resource[] resources = getResources();
				System.out.println("Found [" + resources.length + "] configuration metadata jsons");
				TreeSet<String> names = new TreeSet<>();
				Map<String, ConfigValue> descriptions = new HashMap<>();
				final AtomicInteger count = new AtomicInteger();
				final AtomicInteger matchingPropertyCount = new AtomicInteger();
				final AtomicInteger propertyCount = new AtomicInteger();
				Pattern pattern = Pattern.compile(inclusionPattern);
				ObjectMapper objectMapper = new ObjectMapper();
				for (Resource resource : resources) {
					if (resourceNameContainsPattern(resource)) {
						count.incrementAndGet();
						byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
						Map<String, Object> response = objectMapper.readValue(bytes, HashMap.class);
						List<Map<String, Object>> properties = (List<Map<String, Object>>) response.get("properties");
						properties.forEach(val -> {
							propertyCount.incrementAndGet();
							String name = String.valueOf(val.get("name"));
							if (!pattern.matcher(name).matches()) {
								return;
							}
							Object description = val.get("description");
							Object defaultValue = val.get("defaultValue");
							matchingPropertyCount.incrementAndGet();
							names.add(name);
							descriptions.put(name, new ConfigValue(name, defaultValue == null ? description
									: description + " The default value is " + "`" + defaultValue + "`" + "."));
						});
					}
				}
				System.out.println(
						"Found [" + count + "] Cloud projects configuration metadata jsons. [" + matchingPropertyCount
								+ "/" + propertyCount + "] were matching the pattern [" + inclusionPattern + "]");
				System.out.println("Successfully built the description table");
				if (names.isEmpty()) {
					System.out.println("Will not update the table, since no configuration properties were found!");
					return;
				}

				int[] offset = new int[2];
				String[] key = new String[1];
				descriptions.keySet().stream().max(Comparator.comparingInt(String::length))
						.ifPresent(longest -> offset[0] = longest.length());
				names.stream().max(Comparator.comparingInt(aName -> descriptions.get(aName).getDescription().length()))
						.ifPresent(longest -> key[0] = longest);
				offset[1] = descriptions.get(key[0]).getDescription().trim().length();
				generatePropertiesFiles(outputFile, names, descriptions, offset);
			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		/**
		 * Prefix check.
		 */
		protected boolean resourceNameContainsPattern(Resource resource) {
			try {
				return resource.getURL().toString().contains("cloud");
			}
			catch (Exception e) {
				System.out.println("Exception [" + e + "] for resource [" + resource
						+ "] occurred while trying to retrieve its URL");
				return false;
			}
		}

		/**
		 * Get configuration properties name and descriptions.
		 */
		protected Resource[] getResources() throws IOException {
			return new PathMatchingResourcePatternResolver()
					.getResources("classpath*:/META-INF/spring-configuration-metadata.json");
		}

		protected String getCurrentDate() {
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("MM/dd/yyyy");
			Date date = new Date();
			return sdf.format(date);
		}

		private String paddingWithChar(String printableStr, char c, int maxLength) {
			String trimmedString = printableStr.trim();
			return printableStr + String.join("", Collections.nCopies(maxLength - trimmedString.length(), c + ""));
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
			outputFile = outputFile.substring(outputFile.lastIndexOf("/") + 26, outputFile.lastIndexOf("."))
					.replace('-', ' ');
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

		private void generatePropertiesFiles(String outputFile, TreeSet<String> names,
				Map<String, ConfigValue> descriptions, int[] offset) {
			Path path = Paths.get(outputFile);
			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				writer.write("---");
				writer.newLine();
				writer.write("ms.author: v-yonghuiye");
				writer.newLine();
				writer.write("ms.date: " + getCurrentDate());
				writer.newLine();
				writer.write("---");
				writer.newLine();
				writer.newLine();
				writer.write("## " + generateAnchorName(outputFile) + " properties");
				writer.newLine();
				writer.newLine();
				writer.write("> [!div class=\"mx-tdBreakAll\"]");
				writer.newLine();
				String propertyHeader = paddingWithChar(" Property", ' ', offset[0] + 1);
				String descriptionHeader = paddingWithChar(" Description", ' ', offset[1] + 1);
				writer.write("> |" + propertyHeader + "|" + descriptionHeader + "|");
				writer.newLine();
				String markdownTableCol1 = paddingWithChar("", '-', offset[0] + 2);
				String markdownTableCol2 = paddingWithChar("", '-', offset[1] + 2);
				writer.write("> |" + markdownTableCol1 + "|" + markdownTableCol2 + "|");
				writer.newLine();
				for (String name : names) {
					ConfigValue configValue = descriptions.get(name);
					String nameCol = paddingWithChar(configValue.getName(), ' ', offset[0]);
					String descriptionCol = paddingWithChar(configValue.getDescription(), ' ', offset[1]);
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

	static class ConfigValue {

		public String name;

		public String description;

		ConfigValue(String name, Object description) {
			this.name = name;
			this.description = escapedValue(description);
		}

		private String escapedValue(Object value) {
			return value != null ? value.toString().replaceAll("\\|", "\\\\|") : "";
		}

		public String toString() {
			return "| " + name + " | " + description;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

	}

}
