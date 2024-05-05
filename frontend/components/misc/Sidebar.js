import {useEffect, useState} from 'react';
import {
  DesktopOutlined,
  BookOutlined,
  UserOutlined,
  LogoutOutlined,
  AppstoreOutlined,
  GroupOutlined,
  FunctionOutlined,
  CloudServerOutlined,
  DeploymentUnitOutlined,
  DatabaseOutlined, TeamOutlined,
} from '@ant-design/icons';
import {Layout, Menu} from 'antd';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import Link from 'next/link';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';
import Image from 'next/image';
const {Content, Footer, Sider, Header} = Layout;

export const siteTitle = 'Apollo Tools - Resource Manager';

const Sidebar = ({children}) => {
  const {payload, logout, checkTokenExpired} = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [initialised, setInitialised] = useState(false);

  useEffect(() => {
    setAuthenticated(!checkTokenExpired());
    setInitialised(true);
  }, [children]);

  const onClickLogout = () => {
    logout();
  };

  const getItem = (label, key, children) => {
    return {
      key,
      undefined,
      children,
      label,
    };
  };

  const items = [
    getItem(<Link href="/" ><AppstoreOutlined /><span>Overview</span></Link>, '0'),
    getItem(<><DesktopOutlined /><span>Resources</span></>, '1', [
      getItem(<Link href="/resources/resources" ><CloudServerOutlined /><span>Resources</span></Link>, '1.2'),
      getItem(<Link href="/functions/functions"><FunctionOutlined /><span>Functions</span></Link>, '1.3'),
      getItem(<Link href="/services/services"><DeploymentUnitOutlined /><span>Services</span></Link>, '1.4'),
    ]),
    getItem(<><BookOutlined /><span>Reservations</span></>, '2', [
      getItem(<Link href="/ensembles/ensembles" ><DatabaseOutlined /><span>Ensembles</span></Link>, '2.1'),
      getItem(<Link href="/deployments/deployments" ><GroupOutlined /><span>Deployments</span></Link>, '2.2'),
    ]),
    getItem(<Link href="/accounts/profile" ><UserOutlined /><span>Profile</span></Link>, '3'),
    payload?.role?.[0] === 'admin' ?
        getItem(<Link href="/accounts/accounts"><TeamOutlined /><span>Accounts</span></Link>, '4') : undefined,
    getItem(<div onClick={onClickLogout}><LogoutOutlined /><span>Logout</span></div>, '5'),
  ];

  if (!initialised) {
    return <Layout className="flex justify-center items-center h-screen">
      <LoadingSpinner />
    </Layout>;
  }

  const content = (
    <>
      <Content>
        <main>{children}</main>
      </Content>
      <Footer className="text-center bg-transparent">
        Apollo Tools ©2024
      </Footer>
    </>
  );

  return (
    <Layout hasSider className="min-h-screen">
      {authenticated &&
      <Sider className="h-screen z-[1001] fixed inset-y-0 left-0 mt-14" collapsed={collapsed} onCollapse={(value) => setCollapsed(value)} theme='dark'>
        <Menu theme="dark"
          items={items}
          mode="inline"
          selectable={false}
          openKeys={['1', '2']}
          expandIcon={<></>}
        />
      </Sider>}
      <Header
        className="h-14 pl-3 fixed top-0 z-[1001] w-full flex gap-5
         items-center bg-gradient-to-r from-primary via-primary to-blue-900"
      >
        <Image src="/rm_logo_white.svg" height="60" width="60" alt="Logo"/>
        <div className="w-full text-white text-lg text-start bold">{siteTitle}</div>
      </Header>
      {authenticated ?
        <Layout className="site-layout ml-[200px] mt-16">
          {content}
        </Layout> :
        <Layout className="site-layout mt-16">
          {content}
        </Layout>
      }
    </Layout>
  );
};

Sidebar.propTypes = {
  children: PropTypes.node.isRequired,
};

export default Sidebar;
