import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import { Button, message, Result, Space, Typography } from 'antd';
import {reserveResources} from '../../lib/ReservationService';
import FunctionTable from '../../components/functions/FunctionTable';
import { SmileOutlined } from '@ant-design/icons';

const NewReservation = () => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [selectedResourceIds, setSelectedResourceIds] = useState(new Map());
  const [newReservation, setNewReservation] = useState();
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    if (newReservation != null) {
      messageApi.open({
        type: 'success',
        content: `Reservation with id '${newReservation.reservation_id}' has been created!`,
      });
    }
  }, [newReservation]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onClickReserve = async () => {
    if (!checkTokenExpired()) {
      let requestBody = [];
      selectedResourceIds.forEach((resources, functionId) => {
        resources.forEach((resourceId) => {
          requestBody.push({
            function_id: functionId,
            resource_id: resourceId
          })
        });
      });
      await reserveResources(requestBody, token, setNewReservation, setError);
    }
  };

  const onClickRestart = () => {
    setNewReservation(null);
    setSelectedResourceIds(new Map());
  }

  return (
    <>
      {contextHolder}
      <Head>
        <title>{`${siteTitle}: Resources`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>New Reservation</Typography.Title>
        {newReservation ?
          <Result
              icon={<SmileOutlined />}
              title="The reservation has been created!"
              extra={(<Space size={100}>
                    <Button type="primary">Show</Button>
                    <Button type="default" onClick={onClickRestart}>Restart</Button>
                </Space>
              )}
          />
          :
          <>
            <FunctionTable hideDelete isExpandable selectedResourceIds={selectedResourceIds}
                           setSelectedResourceIds={setSelectedResourceIds}/>
            <Button disabled={selectedResourceIds.size <= 0 } type="primary" onClick={onClickReserve} >Reserve</Button>
          </>
        }
      </div>
    </>
  );
};

export default NewReservation;
