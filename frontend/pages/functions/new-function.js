import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {useEffect, useState} from 'react';
import {Result, Button, Typography} from 'antd';
import {FunctionOutlined, SmileOutlined, UndoOutlined} from '@ant-design/icons';
import NewFunctionForm from '../../components/functions/NewFunctionForm';
import Link from 'next/link';


const NewFunction = () => {
  const [newFunction, setNewFunction] = useState(null);

  useEffect(() => {
    if (newFunction != null) {
      console.log('new function ' + newFunction);
    }
  }, [newFunction]);

  const onClickRestart = () => {
    setNewFunction(null);
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: New Function`}</title>
      </Head>
      <div className="card container w-full md:w-11/12 max-w-7xl p-10 mt-2 mb-2">
        <Typography.Title level={2}>New Function</Typography.Title>
        {newFunction ?
          <Result
            icon={<SmileOutlined />}
            title="The function has been created!"
            extra={
              <>
                <Button type="primary" icon={<UndoOutlined />} onClick={onClickRestart}>Restart</Button>
                <Link href={`/functions/functions`}>
                  <Button type="default" icon={<FunctionOutlined />}>All Functions</Button>
                </Link>
              </>
            }
          /> :
          <NewFunctionForm setNewFunction={setNewFunction} />
        }
      </div>
    </>
  );
};

export default NewFunction;
