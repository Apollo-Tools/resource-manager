import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/function-types`;

/**
 * Create a new function type.
 *
 * @param {string} name the name of the function type
 * @param {string} token the access token
 * @param {function} setFunctionType the function to set the created function type
 * @param {function} setError the function to set the error if one occurred
 */
export async function createFunctionType(name, token, setFunctionType, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
      }),
    });
    const data = await response.json();
    setFunctionType(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all function types.
 *
 * @param {string} token the access token
 * @param {function} setFunctionTypes the function to set the retrieved function types
 * @param {function} setError the function to set the error if one occurred
 */
export async function listFunctionTypes(token, setFunctionTypes, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setFunctionTypes(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete an existing function type.
 *
 * @param {number} id the id of the function type
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteFunctionType(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
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
