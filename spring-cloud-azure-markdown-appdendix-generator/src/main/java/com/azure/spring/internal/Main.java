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

package com.azure.spring.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

		String outputFile = args[0];
		outputFile = outputFile.replace("adoc", "md");
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
				for (Resource resource : resources) {
					if (resourceNameContainsPattern(resource)) {
						count.incrementAndGet();
						byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
						Map<String, Object> response = new ObjectMapper().readValue(bytes, HashMap.class);
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
				names.stream().max(Comparator.comparingInt(aName -> descriptions.get(aName).getDescription().length()))
						.ifPresent(longest -> key[0] = longest);
				offset[0] = descriptions.get(key[0]).getDescription().trim().length();
				descriptions.keySet().stream().max(Comparator.comparingInt(String::length))
						.ifPresent(longest -> offset[1] = longest.length());
				SimpleDateFormat sdf = new SimpleDateFormat();
				sdf.applyPattern("MM/dd/yyyy");
				Date date = new Date();
				Files.write(new File(outputFile).toPath(),
						("---\n" + "ms.author: v-yonghuiye\n" + "ms.date: " + sdf.format(date) + "\n" + "---\n\n"
								+ "> [!div class=\"mx-tdBreakAll\"]\n" + "> | Property"
								+ new String(new char[offset[1] - 7]).replace("\0", " ") + "| Description"
								+ new String(new char[offset[0] - 10]).replace("\0", " ") + "|\n" + "> |"
								+ new String(new char[offset[1] + 2]).replace("\0", "-") + "|"
								+ new String(new char[offset[0] + 2]).replace("\0", "-") + "|\n"
								+ names.stream().map(it -> "> " + addOffset(descriptions.get(it), offset).toString())
										.collect(Collectors.joining(" |\n"))
								+ " |\n").getBytes());

				System.out.println("Successfully stored the output Markdown file!");
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

		/**
		 * Get the value with the longest prefix and description in the object list,
		 * calculate the difference with the prefix and description of other object, and
		 * add spaces for the number of differences to make the table neat.
		 * @param configValue The object to compare
		 * @param offset An array recording the maximum length prefix and description
		 */
		protected ConfigValue addOffset(ConfigValue configValue, int[] offset) {
			int diff1 = offset[1] - configValue.getName().length();
			int diff2 = offset[0] - configValue.getDescription().trim().length();
			String name = configValue.getName() + String.join("", Collections.nCopies(diff1, " "));
			String description = configValue.getDescription() + String.join("", Collections.nCopies(diff2, " "));
			configValue.setName(name);
			configValue.setDescription(description);
			return configValue;
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

		public void setName(String name) {
			this.name = name;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

}
