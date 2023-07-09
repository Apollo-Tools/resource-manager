import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/functions`;

/**
 * Create a new function.
 *
 * @param {number} runtimeId the id of the runtime
 * @param {string} name the name of the function
 * @param {string} code the code of the function
 * @param {string} token the access token
 * @param {function} setFunction the function to set the created function
 * @param {function} setError the function to set the error if one occurs
 */
export async function createFunctionCode(runtimeId, name, code, token, setFunction, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
        runtime: {
          runtime_id: runtimeId,
        },
        code: code,
      }),
    });
    const data = await response.json();
    setFunction(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Create a new function.
 *
 * @param {number} runtimeId the id of the runtime
 * @param {string} name the name of the function
 * @param {File} upload the packaged function
 * @param {string} token the access token
 * @param {function} setFunction the function to set the created function
 * @param {function} setError the function to set the error if one occurs
 */
export async function createFunctionUpload(runtimeId, name, upload, token, setFunction, setError) {
  try {
    const formData = new FormData();
    formData.append('name', name);
    formData.append('runtime', JSON.stringify({runtime_id: runtimeId}));
    formData.append('code', upload);
    const response = await fetch(`${API_ROUTE}/file`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    });
    const data = await response.json();
    setFunction(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all existing functions.
 *
 * @param {string} token the access token
 * @param {function} setFunctions the function to set the retrieved functions
 * @param {function} setError the function to set the error if one occurs
 */
export async function listFunctions(token, setFunctions, setError) {
  try {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setFunctions(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get the details of one function.
 *
 * @param {number} id the id of the function
 * @param {string} token the access token
 * @param {function} setFunction the function to set the retrieved function
 * @param {function} setError the function to set the error if one occurs
 */
export async function getFunction(id, token, setFunction, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setFunction(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Update an existing function.
 *
 * @param {number} id the id of the function
 * @param {string} code the code to update
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function updateFunction(id, code, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        code: code,
      }),
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Update an existing function.
 *
 * @param {number} id the id of the function
 * @param {File} upload the packaged function
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function updateFunctionUpload(id, upload, token, setError) {
  try {
    const formData = new FormData();
    formData.append('code', upload);
    const response = await fetch(`${API_ROUTE}/${id}/file`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete an existing function.
 *
 * @param {number} id the id of the function
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteFunction(id, token, setError) {
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
