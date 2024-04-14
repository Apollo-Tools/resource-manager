import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/platforms`;

/**
 * List all platforms.
 *
 * @param {string} token the access token
 * @param {function} setPlatforms the function to set the retrieved platforms
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listPlatforms(token, setPlatforms, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setPlatforms);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all regions by platform.
 *
 * @param {number} platformId the id of the platform
 * @param {string} token the access token
 * @param {function} setRegions the function to set the retrieved regions
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listRegionsByPlatform(platformId, token, setRegions, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${platformId}/regions`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setRegions);
  };
  await handleApiCall(apiCall, setLoading, setError);
}
