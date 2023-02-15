import {
  CheckCircleTwoTone,
  CloseCircleTwoTone,
  DisconnectOutlined,
  ExclamationCircleFilled,
  InfoCircleOutlined,
} from '@ant-design/icons';
import DateFormatter from '../misc/DateFormatter';
import { Button, Table, Modal, Space } from 'antd';
import { useAuth } from '../../lib/AuthenticationProvider';
import { useEffect, useState } from 'react';
import { cancelReservation, listMyReservations } from '../../lib/ReservationService';
import Link from 'next/link';

const { Column } = Table;
const { confirm } = Modal;

const ReservationTable = () => {
  const { token, checkTokenExpired } = useAuth();
  const [reservations, setReservations] = useState([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listMyReservations(token, setReservations, setError);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onClickDelete = async (id) => {
    if (!checkTokenExpired()) {
      await cancelReservation(id, token, setError)
        .then(() => {
          setReservations((prevReservation) =>
            prevReservation.map((reservation) => {
              if (reservation.reservation_id === id) {
                reservation.is_active = false;
              }
              return reservation;
            }),
          );
        });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled/>,
      content: 'Are you sure you want to cancel this reservation?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickDelete(id);
      },
    });
  };


  return (
    <Table dataSource={ reservations } rowKey={ (record) => record.reservation_id }>
      <Column title="Id" dataIndex="reservation_id" key="id"
              sorter={ (a, b) => a.reservation_id - b.reservation_id }
      />
      <Column title="Is Active" dataIndex="is_active" key="is_active"
              render={ (isActive) => isActive ? <CheckCircleTwoTone twoToneColor="#00ff00" className="text-lg"/> :
                <CloseCircleTwoTone twoToneColor="#ff0000" className="text-lg"/> }
              sorter={ (a, b) => {
                if (a.is_active && b.is_active) return 0;
                else if (a.is_active) return -1;
                else if (b.is_active) return 1;
              }
              }
              defaultSortOrder="ascend"
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
              render={ (createdAt) => <DateFormatter dateTimestamp={ createdAt }/> }
              sorter={ (a, b) => a.created_at - b.created_at }
      />
      <Column title="Action at" key="action"
              render={ (_, record) => (
                <Space size="middle">
                  <Link href={ `/reservations/${ record.reservation_id }` }>
                    <Button icon={ <InfoCircleOutlined/> }/>
                  </Link>
                  <Button onClick={ () => showDeleteConfirm(record.reservation_id) } icon={ <DisconnectOutlined/> }/>
                </Space>
              ) }
      />
    </Table>
  );
};

export default ReservationTable;