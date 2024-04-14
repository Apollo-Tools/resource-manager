import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/vpcs`;

/**
 * List all existing vpc created by the currently logged-in user.
 *
 * @param {string} token the access token
 * @param {function} setVPCs the function to set the retrieved vpcs
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError th function to set the error if one occurred
 */
export async function listVPCs(token, setVPCs, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setVPCs);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Create a new vpc.
 *
 * @param {string} vpcIdValue the id value of the vpc
 * @param {string} subnetIdValue the id value of the subnet
 * @param {number} regionId the id of the region
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the response is valid
 */
export async function createVPC(vpcIdValue, subnetIdValue, regionId, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        vpc_id_value: vpcIdValue,
        subnet_id_value: subnetIdValue,
        region: {
          region_id: regionId,
        },
      }),
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Delete an existing vpc.
 *
 * @param {number} id the id of the vpc
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteVPC(id, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}
