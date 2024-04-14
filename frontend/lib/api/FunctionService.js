import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/functions`;

/**
 * Create a new function.
 *
 * @param {string} name the name of the function
 * @param {number} functionTypeId the id of the function type
 * @param {number} runtimeId the id of the runtime
 * @param {string} code the code of the function
 * @param {number} timeout the timeout of the function
 * @param {number} memory the memory of the function
 * @param {boolean} isPublic whether the public should be publicly available
 * @param {string} token the access token
 * @param {function} setFunction the function to set the created function
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function createFunctionCode(name, functionTypeId, runtimeId, code,
    timeout, memory, isPublic, token, setFunction, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
        function_type: {
          artifact_type_id: functionTypeId,
        },
        runtime: {
          runtime_id: runtimeId,
        },
        code: code,
        timeout_seconds: timeout,
        memory_megabytes: memory,
        is_public: isPublic,
      }),
    });
    await setResult(response, setFunction);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Create a new function.
 *
 * @param {string} name the name of the function
 * @param {number} functionTypeId the id of the function type
 * @param {number} runtimeId the id of the runtime
 * @param {File} upload the packaged function
 * @param {number} timeout the timeout of the function
 * @param {number} memory the memory of the function
 * @param {boolean} isPublic whether the public should be publicly available
 * @param {string} token the access token
 * @param {function} setFunction the function to set the created function
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function createFunctionUpload(name, functionTypeId, runtimeId, upload,
    timeout, memory, isPublic, token, setFunction, setLoading, setError) {
  const apiCall = async () => {
    const formData = new FormData();
    formData.append('name', name);
    formData.append('function_type', JSON.stringify({artifact_type_id: functionTypeId}));
    formData.append('runtime', JSON.stringify({runtime_id: runtimeId}));
    formData.append('timeout_seconds', JSON.stringify(timeout));
    formData.append('memory_megabytes', JSON.stringify(memory));
    formData.append('is_public', JSON.stringify(isPublic));
    formData.append('code', upload);
    const response = await fetch(`${API_ROUTE}/file`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    });
    await setResult(response, setFunction);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all owned functions.
 *
 * @param {string} token the access token
 * @param {function} setFunctions the function to set the retrieved functions
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listMyFunctions(token, setFunctions, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/personal`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setFunctions);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all accessible functions.
 *
 * @param {string} token the access token
 * @param {function} setFunctions the function to set the retrieved functions
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listAllFunctions(token, setFunctions, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setFunctions);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get the details of one function.
 *
 * @param {number} id the id of the function
 * @param {string} token the access token
 * @param {function} setFunction the function to set the retrieved function
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function getFunction(id, token, setFunction, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setFunction);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Update an existing function.
 *
 * @param {number} id the id of the function
 * @param {string} code the code to update
 * @param {number} timeout the timeout of the function
 * @param {number} memory the memory of the function
 * @param {boolean} isPublic whether the public should be publicly available
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function updateFunctionSettings(id, code, timeout, memory, isPublic,
    token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        code: code,
        timeout_seconds: timeout,
        memory_megabytes: memory,
        is_public: isPublic,
      }),
    });
    return await checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Update an existing function.
 *
 * @param {number} id the id of the function
 * @param {File} upload the packaged function
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function updateFunctionUpload(id, upload, token, setLoading, setError) {
  const apiCall = async () => {
    const formData = new FormData();
    formData.append('code', upload);
    const response = await fetch(`${API_ROUTE}/${id}/file`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Delete an existing function.
 *
 * @param {number} id the id of the function
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteFunction(id, token, setLoading, setError) {
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
