import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import ResourceTable from '../../components/resources/ResourceTable';
import {Modal, Typography} from 'antd';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {deleteResource, listResources} from '../../lib/api/ResourceService';
import {ExclamationCircleFilled} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import NewEntityButton from '../../components/misc/NewEntityButton';
import PropTypes from 'prop-types';

const {confirm} = Modal;

const Resources = ({setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState(false);
  const [resources, setResources] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listResources(token, setResources, setLoading, setError);
    }
  }, []);

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteResource(id, token, setLoading, setError)
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
      <div className="default-card">
        <Typography.Title level={2}>All Resources</Typography.Title>
        <NewEntityButton name="Resource"/>
        <ResourceTable resources={resources} onDelete={showDeleteConfirm} hasActions/>
      </div>
    </>
  );
};

Resources.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default Resources;
