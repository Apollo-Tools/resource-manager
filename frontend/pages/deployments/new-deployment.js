import {useEffect, useState} from 'react';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Button, message, Result, Space, Steps, Typography} from 'antd';
import NewDeploymentEnsemble from '../../components/deployments/NewDeploymentEnsemble';
import NewResourceDeployments from '../../components/deployments/NewResourceDeployments';
import AddCredentials from '../../components/deployments/AddCredentials';
import {GroupOutlined, SmileOutlined, UndoOutlined} from '@ant-design/icons';
import Link from 'next/link';
import AddLockResources from '../../components/deployments/AddLockResources';
import PropTypes from 'prop-types';

const steps = [
  {
    title: 'Select ensemble',
  },
  {
    title: 'Select resources',
  },
  {
    title: 'Lock resources',
  },
  {
    title: 'Add credentials',
  },
  {
    title: 'Finished',
  },
];

const NewDeployment = ({setError}) => {
  const [newDeployment, setNewDeployment] = useState();
  const [selectedEnsembleId, setSelectedEnsembleId] = useState();
  const [alertingUrl, setAlertingUrl] = useState();
  const [messageApi, contextHolder] = message.useMessage();
  const [current, setCurrent] = useState(0);
  const [functionResources, setFunctionResources] = useState(new Map());
  const [serviceResources, setServiceResources] = useState(new Map());
  const [lockResources, setLockResources] = useState([]);

  const items = steps.map((item) => ({
    key: item.title,
    title: item.title,
  }));

  const next = () => {
    setCurrent(current + 1);
  };
  const prev = () => {
    setCurrent(current - 1);
  };


  useEffect(() => {
    if (newDeployment != null) {
      messageApi.open({
        type: 'success',
        content: `Deployment with id '${newDeployment.deployment_id}' has been created!`,
      });
    }
  }, [newDeployment]);

  const onClickRestart = () => {
    setCurrent(0);
    setNewDeployment(null);
    setSelectedEnsembleId(null);
    setServiceResources(() => new Map());
    setFunctionResources(() => new Map());
  };

  return (
    <>
      {contextHolder}
      <Head>
        <title>{`${siteTitle}: Resources`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>New Deployment</Typography.Title>
        <Steps current={current} items={items} className="mb-1 p-5 shadow-lg bg-cyan-50"/>
        {current === 0 &&
          <NewDeploymentEnsemble
            selectedEnsemble={selectedEnsembleId}
            alertingUrl={alertingUrl}
            next={next}
            setSelectedEnsemble={setSelectedEnsembleId}
            setAlertingUrl={setAlertingUrl}
          />}
        {current === 1 &&
          <NewResourceDeployments
            functionResources={functionResources}
            serviceResources={serviceResources}
            ensembleId={selectedEnsembleId}
            setFunctionResources={setFunctionResources}
            setServiceResources={setServiceResources}
            next={next}
            prev={prev}
            setError={setError}
          />
        }
        {current === 2 &&
          <AddLockResources
            serviceResources={serviceResources}
            functionResources={functionResources}
            lockResources={lockResources}
            setLockResources={setLockResources}
            next={next}
            prev={prev}
          />
        }
        {current === 3 &&
          <AddCredentials
            serviceResources={serviceResources}
            functionResources={functionResources}
            lockResources={lockResources}
            ensembleId={selectedEnsembleId}
            alertingUrl={alertingUrl}
            next={next}
            prev={prev}
            onSubmit={setNewDeployment}
            setError={setError}
          />
        }
        {current === 4 &&
          <Result
            icon={<SmileOutlined />}
            title="The deployment has been created!"
            extra={(<Space size={100}>
              <Link href={ `/deployments/${ newDeployment?.deployment_id }` }>
                <Button type="primary" icon={<GroupOutlined />}>Show</Button>
              </Link>
              <Button type="default" icon={<UndoOutlined />} onClick={onClickRestart}>Restart</Button>
            </Space>
            )}
          />
        }
      </div>
    </>
  );
};

NewDeployment.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default NewDeployment;
