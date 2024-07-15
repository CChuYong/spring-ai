/*
 * Copyright 2023 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.chat.metadata;

import org.springframework.ai.model.MutableResponseMetadata;

import java.util.Objects;

/**
 * Models common AI provider metadata returned in an AI response.
 *
 * @author John Blum
 * @author Thomas Vitale
 * @since 0.7.0
 */
public class ChatResponseMetadata extends MutableResponseMetadata {

	private static final String AI_METADATA_STRING = "{ id: %2$s, usage: %3$s, rateLimit: %4$s }";

	private String id = ""; // Set to blank to preserve backward compat with previous
							// interface default methods

	private String model = "";

	private RateLimit rateLimit = new EmptyRateLimit();

	private Usage usage = new EmptyUsage();

	private PromptMetadata promptMetadata = PromptMetadata.empty();

	/**
	 * A unique identifier for the chat completion operation.
	 * @return unique operation identifier.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * The model that handled the request.
	 * @return the model that handled the request.
	 */
	public String getModel() {
		return this.model;
	}

	/**
	 * Returns AI provider specific metadata on rate limits.
	 * @return AI provider specific metadata on rate limits.
	 * @see RateLimit
	 */
	public RateLimit getRateLimit() {
		return this.rateLimit;
	}

	/**
	 * Returns AI provider specific metadata on API usage.
	 * @return AI provider specific metadata on API usage.
	 * @see Usage
	 */
	public Usage getUsage() {
		return this.usage;
	}

	/**
	 * Returns the prompt metadata gathered by the AI during request processing.
	 * @return the prompt metadata.
	 */
	public PromptMetadata getPromptMetadata() {
		return this.promptMetadata;
	}

	public static class Builder {

		private final ChatResponseMetadata chatResponseMetadata;

		public Builder() {
			this.chatResponseMetadata = new ChatResponseMetadata();
		}

		public Builder withKeyValue(String key, Object value) {
			this.chatResponseMetadata.put(key, value);
			return this;
		}

		public Builder withId(String id) {
			this.chatResponseMetadata.id = id;
			return this;
		}

		public Builder withModel(String model) {
			this.chatResponseMetadata.model = model;
			return this;
		}

		public Builder withRateLimit(RateLimit rateLimit) {
			this.chatResponseMetadata.rateLimit = rateLimit;
			return this;
		}

		public Builder withUsage(Usage usage) {
			this.chatResponseMetadata.usage = usage;
			return this;
		}

		public Builder withPromptMetadata(PromptMetadata promptMetadata) {
			this.chatResponseMetadata.promptMetadata = promptMetadata;
			return this;
		}

		public ChatResponseMetadata build() {
			return this.chatResponseMetadata;
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ChatResponseMetadata that))
			return false;
		return Objects.equals(id, that.id) && Objects.equals(model, that.model)
				&& Objects.equals(rateLimit, that.rateLimit) && Objects.equals(usage, that.usage)
				&& Objects.equals(promptMetadata, that.promptMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, model, rateLimit, usage, promptMetadata);
	}

	@Override
	public String toString() {
		return AI_METADATA_STRING.formatted(getId(), getUsage(), getRateLimit());
	}

}
