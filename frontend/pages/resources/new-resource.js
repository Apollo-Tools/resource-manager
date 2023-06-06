import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {useState} from 'react';
import {Result, Button, Typography} from 'antd';
import {CloudServerOutlined, SmileOutlined, UndoOutlined} from '@ant-design/icons';
import NewResourceForm from '../../components/resources/NewResourceForm';
import AddMetricValuesForm from '../../components/metrics/AddMetricValuesForm';
import Link from 'next/link';


const NewResource = () => {
  const [newResource, setNewResource] = useState(null);
  const [finished, setFinished] = useState(false);

  const onClickRestart = () => {
    setNewResource(null);
    setFinished(false);
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: New Resource`}</title>
      </Head>
      <div className="card container w-full md:w-11/12 max-w-7xl p-10 mt-2 mb-2">
        <Typography.Title level={2}>New Resource</Typography.Title>
        {finished ?
                    <Result
                      icon={<SmileOutlined />}
                      title="The resource has been created!"
                      extra={
                        <>
                          <Button type="primary" icon={<UndoOutlined />} onClick={onClickRestart}>Restart</Button>
                          <Link href={`/resources/resources`}>
                            <Button type="default" icon={<CloudServerOutlined />}>All Services</Button>
                          </Link>
                        </>
                      }
                    /> :
                    (newResource ?
                    <AddMetricValuesForm resource={newResource} setFinished={setFinished} />:
                    <NewResourceForm setNewResource={setNewResource} />)
        }
      </div>
    </>
  );
};

export default NewResource;
