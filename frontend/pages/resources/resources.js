import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import ResourceTable from '../../components/resources/ResourceTable';
import {Modal, Typography} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import {deleteResource, listResources} from '../../lib/ResourceService';
import {ExclamationCircleFilled} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import NewEntityButton from '../../components/misc/NewEntityButton';

const {confirm} = Modal;

const Resources = () => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [resources, setResources] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResources(token, setResources, setError);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteResource(id, token, setError)
          .then((result) => {
            if (result) {
              setResources(resources.filter((resource) => resource.resource_id !== id));
            }
          });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to delete this resource?',
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
        <title>{`${siteTitle}: Resources`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>All Resources</Typography.Title>
        <NewEntityButton name="Resource"/>
        <ResourceTable resources={resources} onDelete={showDeleteConfirm} hasActions/>
      </div>
    </>
  );
};

export default Resources;
