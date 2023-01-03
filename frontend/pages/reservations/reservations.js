import {siteTitle} from '../../components/Sidebar';
import Head from 'next/head';
import {Button, Modal, Table, Typography} from 'antd';
import DateFormatter from '../../components/DateFormatter';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {listReservations} from '../../lib/ReservaionService';

const {Column} = Table;
const {confirm} = Modal;

const Reservations = () => {
  const {token, checkTokenExpired} = useAuth();
  const [reservations, setReservations] = useState([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listReservations(token, setReservations, setError);
    }
  });

  const onClickDelete = (id) => {
    console.log('delete');
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
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
    <>
      <Head>
        <title>{`${siteTitle}: Reservations`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>My Reservations</Typography.Title>
        <Table dataSource={reservations} rowKey={(record) => record.reservation_id}>
          <Column title="Id" dataIndex="resource_id" key="id"
            sorter={(a, b) => a.reservation_id - b.reservation_id}
            defaultSortOrder="ascend"
          />
          <Column title="Created at" dataIndex="created_at" key="created_at"
            render={(createdAt) => <DateFormatter dateString={createdAt}/>}
            sorter={(a, b) => a.created_at - b.created_at}
          />
          <Column title="Action at" key="action"
            render={(_, record) => (
              <Button onClick={() => showDeleteConfirm(record.reservation_id)} icon={<DeleteOutlined />}/>
            )}
          />
        </Table>
      </div>
    </>
  );
};

export default Reservations;
