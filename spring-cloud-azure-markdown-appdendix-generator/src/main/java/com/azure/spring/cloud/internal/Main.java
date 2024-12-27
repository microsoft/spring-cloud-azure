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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

/**
 * Generate markdown files with configuration properties based on the path of the file to
 * be output and the pattern included in the properties.
 */
public final class Main {

	private Main() {
	}

	/**
	 * This method is used to generate configuration properties Markdown files.
	 * @param args args[0]: output file name. The file name must start with
	 * "_configuration-properties-". And in actual outputted file name, ".adoc" will be
	 * replaced by ".md", "_" will be replaced by "". For example:
	 * "_configuration-properties-global.adoc" after the replacement is
	 * "configuration-properties-global.md". This is required because this module and the
	 * docs' module use the configuration of the parent pom and changes to configuration
	 * properties need to be synced to MS Docs. args[1]: property inclusion pattern.
	 * Filter unwanted properties based on patterns.
	 */
	public static void main(String... args) {
		String outputFile = args[0].replace("adoc", "md").replace("_", "");
		String inclusionPattern = args.length > 1 ? args[1] : ".*";
		File parent = new File(outputFile).getParentFile();
		if (!parent.exists()) {
			System.out.println(
					"No parent directory [" + parent + "] found. Will not generate the configuration properties file");
			return;
		}
		new Generator().generate(outputFile, inclusionPattern, getCurrentDateString());
	}

	/**
	 * Get current date string.
	 * @return the current date string
	 */
	static String getCurrentDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		simpleDateFormat.applyPattern("MM/dd/yyyy");
		Date date = new Date();
		return simpleDateFormat.format(date);
	}

	/**
	 * Generate Markdown files based on properties and filename.
	 */
	static class Generator {

		/**
		 * Generate files prefixed with configuration-properties.
		 * @param outputFile the file name must start with "configuration-properties-"
		 * @param inclusionPattern the inclusion pattern
		 * @param date the current date string
		 */
		void generate(String outputFile, String inclusionPattern, String date) {
			try {
				System.out.println("Parsing all configuration metadata");
				List<Resource> resources = getSpringConfigurationMetadataJsonFilesInClasspath();
				TreeMap<String, String> descriptions = new TreeMap<>();
				int propertyCount = 0;
				Pattern pattern = Pattern.compile(inclusionPattern);
				ObjectMapper objectMapper = new ObjectMapper();
				for (Resource resource : resources) {
					byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
					Map<String, Object> rootMap = objectMapper.readValue(bytes, HashMap.class);
					List<Map<String, Object>> properties = (List<Map<String, Object>>) rootMap.get("properties");
					propertyCount += properties.size();
					properties.forEach(propertyItem -> {
						String name = String.valueOf(propertyItem.get("name"));
						if (!pattern.matcher(name).matches()) {
							return;
						}
						String description = formatDescription(String.valueOf(propertyItem.get("description")));
						String defaultValue = String.valueOf(propertyItem.get("defaultValue"));
						descriptions.put(name, defaultValue == null || defaultValue.equals("null") ? description
								: description + " The default value is " + "`" + defaultValue + "`" + ".");
					});
				}
				System.out.println("Found [" + resources.size() + "] Azure projects configuration metadata jsons. ["
						+ descriptions.size() + "/" + propertyCount + "] were matching the pattern [" + inclusionPattern
						+ "]");
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
		 * Get Spring on Azure related "spring-configuration-metadata.json" files in
		 * classpath.
		 * @return a result set containing the specified fields
		 * @throws IOException did not get any "spring-configuration-metadata.json" files
		 * in classpath
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

		private String formatDescription(String description) {
			return formatURL(replaceQuotes(description)).replace("GrantedAuthority's", "`GrantedAuthority`'s")
					.replace("access_token's", "`access_token`'s");
		}

		private String formatURL(String description) {
			String regex1 = "(\\b\\w+\\b)\\s?:\\s*https?://learn.microsoft.com([^\\s]+)";
			String regex2 = "(@see <a href=\")https?://learn.microsoft.com([^\\s]+)";
			return description.replaceAll(regex1, "[$1]($2).").replaceAll(regex2, "$1$2");
		}

		/**
		 * Replace single/double quotes with backticks, ignoring possessive and nested
		 * cases.
		 */
		private String replaceQuotes(String description) {
			StringBuilder result = new StringBuilder();
			int n = description.length();
			boolean insideSingleQuote = false;
			boolean insideDoubleQuote = false;
			boolean insideBacktick = false;
			boolean insideSeeTag = false;

			for (int i = 0; i < n; i++) {
				char currentChar = description.charAt(i);
				if (!insideSeeTag && i >= 5 && description.startsWith("@see <", i - 5)) {
					insideSeeTag = true;
				}
				if (insideSeeTag) {
					result.append(currentChar);
					if (currentChar == '>') {
						insideSeeTag = false;
					}
					continue;
				}
				if (currentChar == '`') {
					insideBacktick = !insideBacktick;
					result.append(currentChar);
					continue;
				}
				if (!insideBacktick) {
					// skip 's, n't
					if (i + 1 < n && currentChar == '\''
							&& (description.charAt(i + 1) == 's' || description.charAt(i + 1) == 't') && i > 0
							&& (Character.isLetter(description.charAt(i - 1)) || description.charAt(i - 1) == 'n')) {
						result.append(currentChar);
						continue;
					}
					if (currentChar == '\'' && !insideDoubleQuote) {
						result.append('`');
						insideSingleQuote = !insideSingleQuote;
					}
					else if (currentChar == '"' && !insideSingleQuote) {
						result.append('`');
						insideDoubleQuote = !insideDoubleQuote;
					}
					else {
						result.append(currentChar);
					}
				}
				else {
					result.append(currentChar);
				}
			}
			return result.toString();
		}

		/**
		 * Padding the string to the required length.
		 * @param string the string to padding
		 * @param c the padding content
		 * @param resultLength the length of padding
		 * @return the required length
		 */
		private String paddingWithChar(String string, char c, int resultLength) {
			String result = String.format("%1$-" + resultLength + "s", string.trim());
			if (' ' != c) {
				return result.replace(' ', c);
			}
			return result;
		}

		/**
		 * Uppercase string.
		 * @param letters the uppercase string required
		 * @return the uppercase string
		 */
		private String uppercaseString(String letters) {
			if ("db".equals(letters) | "jms".equals(letters) | "b2c".equals(letters)) {
				return letters.toUpperCase();
			}
			else {
				return letters.substring(0, 1).toUpperCase().concat(letters.substring(1));
			}
		}

		/**
		 * Generate title name for markdown files.
		 * @param outputFile the output file name
		 * @return the title name
		 */
		private String generateTitleName(String outputFile) {
			outputFile = outputFile
					.substring(outputFile.lastIndexOf(File.separator) + "_configuration-properties-".length(),
							outputFile.lastIndexOf("."))
					.replace('-', ' ').trim();
			if ("all".equals(outputFile)) {
				return "List of configuration";
			}
			String[] value = outputFile.split(" ");
			StringBuilder result = new StringBuilder(" ");
			for (String s : value) {
				result.append(uppercaseString(s)).append(" ");
			}
			return result.toString().trim();
		}

		/**
		 * Compare the lengths of elements in the given result set and return the max
		 * length.
		 * @param stringStream the result sets to compare
		 * @return the max length
		 */
		private int getMaxLength(Stream<String> stringStream) {
			return stringStream.map(String::trim).mapToInt(String::length).max().orElse(0);
		}

		private int getDescriptionColumnWidth(String outputFile, Stream<String> stringStream) {
			Map<String, Integer> DES_WIDTH = new HashMap<>();
			DES_WIDTH.put("configuration-properties-azure-active-directory.md", 641);
			DES_WIDTH.put("configuration-properties-azure-active-directory-b2c.md", 470);
			DES_WIDTH.put("configuration-properties-azure-service-bus.md", 194);
			DES_WIDTH.put("configuration-properties-azure-service-bus-jms.md", 446);
			return DES_WIDTH.getOrDefault(outputFile.substring(outputFile.lastIndexOf("/") + 1),
					getMaxLength(stringStream));
		}

		private String getServiceName(String outputFile) {
			HashMap<String, String> DES_WIDTH = new HashMap<>();
			DES_WIDTH.put("configuration-properties-all.md", "Spring Cloud Azure");
			DES_WIDTH.put("configuration-properties-azure-active-directory.md", "Microsoft Entra");
			DES_WIDTH.put("configuration-properties-azure-active-directory-b2c.md", "Azure Active Directory B2C");
			DES_WIDTH.put("configuration-properties-azure-app-configuration.md", "Azure App Configuration");
			DES_WIDTH.put("configuration-properties-azure-cosmos-db.md", "Azure Cosmos DB");
			DES_WIDTH.put("configuration-properties-azure-event-hubs.md", "Azure Event Hubs");
			DES_WIDTH.put("configuration-properties-azure-key-vault.md", "Azure Key Vault");
			DES_WIDTH.put("configuration-properties-azure-key-vault-certificates.md", "Azure Key Vault Certificates");
			DES_WIDTH.put("configuration-properties-azure-key-vault-secrets.md", "Azure Key Vault Secrets");
			DES_WIDTH.put("configuration-properties-azure-service-bus.md", "Azure Service Bus");
			DES_WIDTH.put("configuration-properties-azure-service-bus-jms.md", "Azure Service Bus JMS");
			DES_WIDTH.put("configuration-properties-azure-storage.md", "Azure Storage");
			DES_WIDTH.put("configuration-properties-azure-storage-blob.md", "Azure Storage Blob");
			DES_WIDTH.put("configuration-properties-azure-storage-file-share.md", "Azure Storage File Share");
			DES_WIDTH.put("configuration-properties-azure-storage-queue.md", "Azure Storage Queue");
			DES_WIDTH.put("configuration-properties-global.md", "Spring Cloud Azure Global");
			return DES_WIDTH.getOrDefault(outputFile.substring(outputFile.lastIndexOf("/") + 1), null);
		}

		/**
		 * Generate properties markdown files.
		 * @param outputFile the output file
		 * @param descriptions the result set of properties and their descriptions
		 * @param date the current date string
		 */
		private void generatePropertiesFile(String outputFile, TreeMap<String, String> descriptions, String date) {
			int nameColumnWidth = getMaxLength(descriptions.keySet().stream());
			int descriptionColumnWidth = getDescriptionColumnWidth(outputFile, descriptions.values().stream());
			Path path = Paths.get(outputFile);
			String serviceName = getServiceName(outputFile);
			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				writer.write("---");
				writer.newLine();
				writer.write(String.format("title: %s configuration properties", serviceName));
				writer.newLine();
				writer.write(String.format("description: This reference doc contains all %s configuration properties.",
						serviceName));
				writer.newLine();
				writer.write("author: KarlErickson");
				writer.newLine();
				writer.write("ms.author: hangwan");
				writer.newLine();
				writer.write("ms.date: " + date);
				writer.newLine();
				writer.write("ms.topic: reference");
				writer.newLine();
				writer.write("ms.custom: devx-track-java, spring-cloud-azure, devx-track-extended-java");
				writer.newLine();
				writer.write("---");
				writer.newLine();
				writer.newLine();
				writer.write(String.format("# %s configuration properties", serviceName));
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
				for (Map.Entry<String, String> description : descriptions.entrySet()) {
					String nameCol = paddingWithChar(description.getKey(), ' ', nameColumnWidth);
					String descriptionCol = paddingWithChar(description.getValue(), ' ', descriptionColumnWidth);
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
