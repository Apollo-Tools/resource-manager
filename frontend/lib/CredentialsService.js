const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/credentials`;

export async function createCredentials(providerId, accessKey, secretAccessKey, sessionToken, token,
    setCredentials, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        access_key: accessKey,
        resource_provider: {
          provider_id: providerId,
        },
        secret_access_key: secretAccessKey,
        session_token: sessionToken,
      }),
    });
    const data = await response.json();
    setCredentials(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}


export async function listCredentials(token, setCredentials, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setCredentials(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function deleteCredentials(id, token, setError) {
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
