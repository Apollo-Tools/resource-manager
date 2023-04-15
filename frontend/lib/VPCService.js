import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/vpcs`;

export async function listVPCs(token, setVPCs, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setVPCs(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function createVPC(vpcIdValue, subnetIdValue, regionId, token, setVPC, setError) {
  try {
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
    const data = await response.json();
    setVPC(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function deleteVPC(id, token, setError) {
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
