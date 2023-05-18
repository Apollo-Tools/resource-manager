import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/regions`;

/**
 * List all regions.
 *
 * @param {string} token the access token
 * @param {function} setRegions the function to set the retrieved regions
 * @param {function} setError the function to set the error if one occurred
 */
export async function listRegions(token, setRegions, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setRegions(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
