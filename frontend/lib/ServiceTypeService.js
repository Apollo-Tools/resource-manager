import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/service-types`;

/**
 * List all supported service types.
 *
 * @param {string} token the access token
 * @param {function} setServiceTypes the function to set the retrieved service types
 * @param {function} setError the function to set the error if one occurred
 */
export async function listServiceTypes(token, setServiceTypes, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setServiceTypes(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
