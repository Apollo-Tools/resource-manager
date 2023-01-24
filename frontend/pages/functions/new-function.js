import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {useEffect, useState} from 'react';
import {Result, Button, Typography} from 'antd';
import {SmileOutlined} from '@ant-design/icons';
import NewFunctionForm from '../../components/functions/NewFunctionForm';
import AddFunctionResourcesForm from '../../components/functions/AddFunctionResourcesForm';


const NewFunction = () => {
  const [newFunction, setNewFunction] = useState(null);
  const [finished, setFinished] = useState(false);

  useEffect(() => {
    if (newFunction != null) {
      console.log('new function ' + newFunction);
    }
  }, [newFunction]);

  const onClickRestart = () => {
    setNewFunction(null);
    setFinished(false);
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: New Function`}</title>
      </Head>
      <div className="card container w-full md:w-11/12 max-w-7xl p-10 mt-2 mb-2">
        <Typography.Title level={2}>New Function</Typography.Title>
        {finished ?
          <Result
            icon={<SmileOutlined />}
            title="The function has been created!"
            extra={<Button type="primary" onClick={onClickRestart}>Restart</Button>}
          />:
          (newFunction ?
            <AddFunctionResourcesForm func={newFunction} setFinished={setFinished} isSkipable/>:
            <NewFunctionForm setNewFunction={setNewFunction} />)
        }
      </div>
    </>
  );
};

export default NewFunction;
