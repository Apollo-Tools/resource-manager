import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/functions`;

/**
 * List all resources that are linked to a function
 *
 * @param {number} functionId the id of the function
 * @param {string} token the access token
 * @param {function} setFunctionResources the function to set the retrieved function resources
 * @param {function} setError the function to set the error if one occurs
 */
export async function listFunctionResources(functionId, token, setFunctionResources, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${functionId}/resources`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setFunctionResources(data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Add resources to a function.
 *
 * @param {number} functionId the id of the function
 * @param {List<*>} resources the resources
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 */
export async function addFunctionResources(functionId, resources, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${functionId}/resources`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(resources),
    });
    await response;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete an existing function resource
 *
 * @param {number} functionId the id of the function
 * @param {number} resourceId the id of the resource
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteFunctionResource(functionId, resourceId, token, setError) {
  try {
    const response = await fetch(
        `${API_ROUTE}/${functionId}/resources/${resourceId}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
