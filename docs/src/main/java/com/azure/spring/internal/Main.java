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
 *
 */
public final class Main {

	private Main() {
	}

	public static void main(String... args) {

		String outputFile = args[0];
		String inclusionPattern = args.length > 1 ? args[1] : ".*";
		String path = System.getProperty("user.dir");
		String outputFileAdoc = path + "\\src\\main\\asciidoc\\" + outputFile + ".adoc";
		String outputFileMarkdown = path + "\\src\\main\\markdown\\" + outputFile + ".md";

		File parentAsciidoc = new File(outputFileAdoc).getParentFile();
		if (!parentAsciidoc.exists()) {
			System.out.println("No asciidoc parent directory [" + parentAsciidoc.toString()
					+ "] found. Will not generate the configuration properties file");
			return;
		}

		File parentMarkdown = new File(outputFileMarkdown).getParentFile();
		if (!parentAsciidoc.exists()) {
			System.out.println("No markdown parent directory [" + parentMarkdown.toString()
					+ "] found. Will not generate the configuration properties file");
			return;
		}

		new Generator().generate(outputFileAdoc, inclusionPattern);

		new Generator().generate(outputFileMarkdown, inclusionPattern);
	}

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

				if (outputFile.endsWith(".adoc")) {
					Files.write(new File(outputFile).toPath(),
							("|===\n" + "|Name | Description\n\n" + names.stream()
									.map(it -> descriptions.get(it).toString()).collect(Collectors.joining("\n"))
									+ "\n\n" + "|===").getBytes());
					System.out.println("Successfully stored the output AsciiDoc file!");
				}

				if (outputFile.endsWith(".md")) {
					outputFile = outputFile.replace("_", "-");
					String str = outputFile.substring(outputFile.lastIndexOf("\\") + 1, outputFile.length() - 1);
					outputFile = outputFile.replace(str, "appendix" + str);

					Files.write(new File(outputFile).toPath(),
							("> [!div class=\"mx-tdBreakAll\"]\n" + "> |Property | Description|\n"
									+ "> |---------|------------|\n"
									+ names.stream().map(it -> "> " + descriptions.get(it).toString())
											.collect(Collectors.joining(" |\n"))
									+ " |\n").getBytes());
					System.out.println("Successfully stored the output Markdown file!");
				}
			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

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

		protected Resource[] getResources() throws IOException {
			return new PathMatchingResourcePatternResolver()
					.getResources("classpath*:/META-INF/spring-configuration-metadata.json");
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
			return "|" + name + " | " + description;
		}

	}

}
