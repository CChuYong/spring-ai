/*
 * Copyright 2023-2024 the original author or authors.
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

package org.springframework.ai.evaluation;

import java.util.Collections;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Implementation of {@link Evaluator} used to evaluate the factual accuracy of Large
 * Language Model (LLM) responses against provided context.
 * <p>
 * This evaluator addresses a specific type of potential error in LLM outputs known as
 * "hallucination" in the context of grounded factuality. It verifies whether a given
 * statement (the "claim") is logically supported by a provided context (the "document").
 * <p>
 * Key concepts: - Document: The context or grounding information against which the claim
 * is checked. - Claim: The statement to be verified against the document.
 * <p>
 * The evaluator uses a prompt-based approach with a separate, typically smaller and more
 * efficient LLM to perform the fact-checking. This design choice allows for
 * cost-effective and rapid verification, which is crucial when evaluating longer LLM
 * outputs that may require multiple verification steps.
 * <p>
 * Implementation note: For efficient and accurate fact-checking, consider using
 * specialized models like Bespoke-Minicheck, a grounded factuality checking model
 * developed by Bespoke Labs and available in Ollama. Such models are specifically
 * designed to fact-check responses generated by other models, helping to detect and
 * reduce hallucinations. For more information, see:
 * <a href="https://ollama.com/blog/reduce-hallucinations-with-bespoke-minicheck">Reduce
 * Hallucinations with Bespoke-Minicheck</a> and the research paper:
 * <a href="https://arxiv.org/pdf/2404.10774v1">MiniCheck: An Efficient Method for LLM
 * Hallucination Detection</a>
 * <p>
 * Note: This evaluator is specifically designed to fact-check statements against given
 * information. It's not meant for other types of accuracy tests, like quizzing an AI on
 * obscure facts without giving it any reference material to work with (so-called 'closed
 * book' scenarios).
 * <p>
 * The evaluation process aims to determine if the claim is supported by the document,
 * returning a boolean result indicating whether the fact-check passed or failed.
 *
 * @author Eddú Meléndez
 * @author Mark Pollack
 * @see Evaluator
 * @see EvaluationRequest
 * @see EvaluationResponse
 * @since 1.0.0
 */
public class FactCheckingEvaluator implements Evaluator {

	private static final String DEFAULT_EVALUATION_PROMPT_TEXT = """
				Evaluate whether or not the following claim is supported by the provided document.
				Respond with "yes" if the claim is supported, or "no" if it is not.
				Document: \\n {document}\\n
				Claim: \\n {claim}
			""";

	private static final String BESPOKE_EVALUATION_PROMPT_TEXT = """
				Document: \\n {document}\\n
				Claim: \\n {claim}
			""";

	private final ChatClient.Builder chatClientBuilder;

	private final String evaluationPrompt;

	/**
	 * Constructs a new FactCheckingEvaluator with the provided ChatClient.Builder. Uses
	 * the default evaluation prompt suitable for general purpose LLMs.
	 * @param chatClientBuilder The builder for the ChatClient used to perform the
	 * evaluation
	 */
	public FactCheckingEvaluator(ChatClient.Builder chatClientBuilder) {
		this(chatClientBuilder, DEFAULT_EVALUATION_PROMPT_TEXT);
	}

	/**
	 * Constructs a new FactCheckingEvaluator with the provided ChatClient.Builder and
	 * evaluation prompt.
	 * @param chatClientBuilder The builder for the ChatClient used to perform the
	 * evaluation
	 * @param evaluationPrompt The prompt text to use for evaluation
	 */
	public FactCheckingEvaluator(ChatClient.Builder chatClientBuilder, String evaluationPrompt) {
		this.chatClientBuilder = chatClientBuilder;
		this.evaluationPrompt = evaluationPrompt;
	}

	/**
	 * Creates a FactCheckingEvaluator configured for use with the Bespoke Minicheck
	 * model.
	 * @param chatClientBuilder The builder for the ChatClient used to perform the
	 * evaluation
	 * @return A FactCheckingEvaluator configured for Bespoke Minicheck
	 */
	public static FactCheckingEvaluator forBespokeMinicheck(ChatClient.Builder chatClientBuilder) {
		return new FactCheckingEvaluator(chatClientBuilder, BESPOKE_EVALUATION_PROMPT_TEXT);
	}

	/**
	 * Evaluates whether the response content in the EvaluationRequest is factually
	 * supported by the context provided in the same request.
	 * @param evaluationRequest The request containing the response to be evaluated and
	 * the supporting context
	 * @return An EvaluationResponse indicating whether the claim is supported by the
	 * document
	 */
	@Override
	public EvaluationResponse evaluate(EvaluationRequest evaluationRequest) {
		var response = evaluationRequest.getResponseContent();
		var context = doGetSupportingData(evaluationRequest);

		String evaluationResponse = this.chatClientBuilder.build()
			.prompt()
			.user(userSpec -> userSpec.text(evaluationPrompt).param("document", context).param("claim", response))
			.call()
			.content();

		boolean passing = evaluationResponse.equalsIgnoreCase("yes");
		return new EvaluationResponse(passing, "", Collections.emptyMap());
	}

}
