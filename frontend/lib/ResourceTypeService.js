import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/resource-types`;

/**
 * List all existing resource types.
 *
 * @param {string} token the access token
 * @param {function} setResourceTypes the function to set the retrieved resource types
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResourceTypes(token, setResourceTypes, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResourceTypes(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
