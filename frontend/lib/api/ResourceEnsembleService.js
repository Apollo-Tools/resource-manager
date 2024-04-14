import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/ensembles`;

/**
 * Add a resource to an existing ensemble.
 *
 * @param {number} ensembleId the id of the ensemble
 * @param {boolean} resourceId the id of the resource
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function addResourceToEnsemble(ensembleId, resourceId, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${ensembleId}/resources/${resourceId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Remove a resource from an ensemble.
 *
 * @param {number} ensembleId the id of the ensemble
 * @param {boolean} resourceId the id of the resource
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteResourceFromEnsemble(ensembleId, resourceId, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${ensembleId}/resources/${resourceId}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}
