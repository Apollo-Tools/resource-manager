import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {useEffect, useState} from 'react';
import {Result, Button, Typography} from 'antd';
import {UndoOutlined, SmileOutlined, DeploymentUnitOutlined} from '@ant-design/icons';
import NewServiceForm from '../../components/services/NewServiceForm';
import Link from 'next/link';


const NewService = () => {
  const [newService, setNewService] = useState(null);

  useEffect(() => {
    if (newService != null) {
      console.log('new function ' + newService);
    }
  }, [newService]);

  const onClickRestart = () => {
    setNewService(null);
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: New Service`}</title>
      </Head>
      <div className="card container w-full md:w-11/12 max-w-7xl p-10 mt-2 mb-2">
        <Typography.Title level={2}>New Service</Typography.Title>
        {newService != null ?
          <Result
            icon={<SmileOutlined />}
            title="The service has been created!"
            extra={
              <>
                <Button type="primary" icon={<UndoOutlined />} onClick={onClickRestart}>Restart</Button>
                <Link href={`/services/services`}>
                  <Button type="default" icon={<DeploymentUnitOutlined />}>All Services</Button>
                </Link>
              </>
            }
          />:
          <NewServiceForm setNewService={setNewService} />
        }
      </div>
    </>
  );
};

export default NewService;
