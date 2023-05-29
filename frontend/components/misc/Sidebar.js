import {useEffect, useState} from 'react';
import {
  DesktopOutlined,
  BookOutlined,
  UserOutlined,
  LogoutOutlined,
  HomeOutlined,
  GroupOutlined,
  FunctionOutlined,
  CloudServerOutlined,
  DeploymentUnitOutlined,
  DatabaseOutlined,
} from '@ant-design/icons';
import {Layout, Menu} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import Link from 'next/link';
import PropTypes from 'prop-types';
const {Content, Footer, Sider} = Layout;

export const siteTitle = 'Apollo Tools - Resource Manager';

const Sidebar = ({children}) => {
  const {logout, checkTokenExpired} = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    setAuthenticated(!checkTokenExpired());
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
    getItem(<Link href="/" ><HomeOutlined /><span>Home</span></Link>, '0'),
    getItem(<><DesktopOutlined /><span>Resources</span></>, '1', [
      getItem(<Link href="/resources/resources" ><CloudServerOutlined /><span>Resources</span></Link>, '1.2'),
      getItem(<Link href="/functions/functions"><FunctionOutlined /><span>Functions</span></Link>, '1.3'),
      getItem(<Link href="/services/services"><DeploymentUnitOutlined /><span>Services</span></Link>, '1.4'),
    ]),
    getItem(<><BookOutlined /><span>Reservations</span></>, '2', [
      getItem(<Link href="/ensembles/ensembles" ><DatabaseOutlined /><span>Ensembles</span></Link>, '2.1'),
      getItem(<Link href="/reservations/reservations" ><GroupOutlined /><span>Reservations</span></Link>, '2.2'),
    ]),
    getItem(<Link href="/accounts/profile" ><UserOutlined /><span>Profile</span></Link>, '3'),
    getItem(<div onClick={onClickLogout}><LogoutOutlined /><span>Logout</span></div>, '4'),
  ];

  return (
    <Layout className="min-h-screen"
    >
      {authenticated &&
      <Sider collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
        <Menu theme="dark"
          items={items}
          mode="inline"
          className="mt-2"
          selectable={false}
          openKeys={['1', '2']}
          expandIcon={<></>}
        />
      </Sider>}
      <Layout className="site-layout">
        <div
          className="h-12 flex justify-center items-center p-0 bg-primary"
        >
          <div className="text-white text-sm md:text-xl text-center font-semibold">{siteTitle}</div>
        </div>
        <Content className="md:ml-4">
          <main>{children}</main>
        </Content>
        <Footer
          className="text-center"
        >
          Apollo Tools ©2022
        </Footer>
      </Layout>
    </Layout>
  );
};

Sidebar.propTypes = {
  children: PropTypes.node.isRequired,
};

export default Sidebar;
