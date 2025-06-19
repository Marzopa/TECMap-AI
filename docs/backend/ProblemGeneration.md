This endpoint is available at `/ai/problem/`.
# Parameters:
| Parameter    | Description                                                                                                                 | Example |
|--------------|-----------------------------------------------------------------------------------------------------------------------------|---------|
| `topic`      | A string representing the main topic of the problem                                                                         | arrays  |
| `difficulty` | An integer representing difficulty, as defined [here](../../backend/src/main/resources/Modelfiles/cs-problemGenerator.txt). | 3       |

## Optional parameters:
| Parameter          | Description                                                                                                                       | Example                 |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------|-------------------------|
| `additionalTopics` | A list of strings representing possible additional topics to include. There is no guarantee that any or all of them will be used. | ["recursion", "loops"]  |
| `excludedTopics`   | A list of strings containing topics not desired in the program. For the most part, you can rely that these will not be present.   | ["dynamic programming"] |
| `method`           | One of the methods explained [here](#generation-methods). It defaults to `default`.                                               | chase                   |

# Generation methods
## `default`
This method uses the following steps:
- It first generates a problem using the `cs-problemGenerator` model.
- It then uses `cs-smallProblemSolver` to generate a preliminary solution to the problem. This solution is only used for more accurate topic scanning.
- Finally, `cs-topicScanner` generates the tags the solver used in its solution, which should translate to the topics of the problem.

## `chase`

## `openai`
This method follows the exact same steps as `default`, but uses OpenAI models instead of Ollama. The models used are:
- `cs-problemGenerator` -> `gpt-4.1-2025-04-14`
- `cs-smallProblemSolver` -> `gpt-4.1-nano-2025-04-14`
- `cs-topicScanner` -> `gpt-4.1-nano-2025-04-14`

There is currently no way to use these models from the containers, so you will have to run the backend locally with the OpenAI key set as an environment variable called `OPENAI_API_KEY`.