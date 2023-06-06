import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/platforms`;

/**
 * List all platforms.
 *
 * @param {string} token the access token
 * @param {function} setPlatforms the function to set the retrieved platforms
 * @param {function} setError the function to set the error if one occurred
 */
export async function listPlatforms(token, setPlatforms, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setPlatforms(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all regions by platform.
 *
 * @param {number} platformId the id of the platform
 * @param {string} token the access token
 * @param {function} setRegions the function to set the retrieved regions
 * @param {function} setError the function to set the error if one occurred
 */
export async function listRegionsByPlatform(platformId, token, setRegions, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${platformId}/regions`, {
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
