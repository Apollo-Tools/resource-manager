import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {listResources} from '../../lib/ResourceService';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Button, message, Typography} from 'antd';
import {reserveResources} from '../../lib/ReservationService';
import ResourceTable from '../../components/resources/ResourceTable';
import {listFunctions} from '../../lib/FunctionService';
import FunctionTable from '../../components/functions/FunctionTable';

const NewReservation = () => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [functions, setFunctions] = useState([]);
  const [resources, setResources] = useState([]);
  const [selectedResourceIds, setSelectedResourceIds] = useState([]);
  const [newReservation, setNewReservation] = useState();
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResources(true, token, setResources, setError);
      listFunctions(token, setFunctions, setError)
          .then(setFunctions((prevFunctions) =>
            prevFunctions.sort((a, b) => a.name.localeCompare(b.name))));
    }
  }, []);

  useEffect(() => {
    if (newReservation != null) {
      messageApi.open({
        type: 'success',
        content: `Reservation with id '${newReservation.reservation_id}' has been created!`,
      });
      setNewReservation(null);
    }
  }, [newReservation]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onClickReserve = () => {
    if (!checkTokenExpired()) {
      reserveResources(selectedResourceIds, token, setNewReservation, setError)
          .then(() => {
            listResources(true, token, setResources, setError)
                .then(() => setSelectedResourceIds([]));
          });
    }
  };

  const rowSelection = {
    selectedResourceIds,
    onChange: (newSelectedResourceIds) => {
      setSelectedResourceIds(newSelectedResourceIds);
    },
  };

  return (
    <>
      {contextHolder}
      <Head>
        <title>{`${siteTitle}: Resources`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>New Reservation</Typography.Title>
        <ResourceTable resources={resources} hasActions rowSelection={rowSelection} />
        <FunctionTable isExpandable />
        <Button disabled={selectedResourceIds.length <= 0 } type="primary" onClick={onClickReserve} >Reserve</Button>
      </div>
    </>
  );
};

export default NewReservation;
