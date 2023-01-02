import {useState} from 'react';
import {
  DesktopOutlined,
  BookOutlined,
  UserOutlined,
  LogoutOutlined,
  HomeOutlined,
  PlusSquareOutlined,
  GroupOutlined,
} from '@ant-design/icons';
import {Layout, Menu} from 'antd';
import {useAuth} from '../lib/AuthenticationProvider';
import Link from 'next/link';
import PropTypes from 'prop-types';
const {Content, Footer, Sider} = Layout;

export const siteTitle = 'Apollo Tools - Resource Manager';

const Sidebar = ({children}) => {
  const {logout} = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [selectedKey, setSelectedKey] = useState('0');

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
      getItem(<Link href="/resources/resources" ><GroupOutlined /><span>All Resources</span></Link>, '1.1'),
      getItem(<Link href="/resources/new-resource" ><PlusSquareOutlined /><span>New Resource</span></Link>, '1.2'),
    ]),
    getItem(<><BookOutlined /><span>Reservations</span></>, '2', [
      getItem(<Link href="/reservations/reservations" ><GroupOutlined /><span>All Reservations</span></Link>, '2.1'),
      getItem(<Link href="/reservations/new-reservation" ><PlusSquareOutlined /><span>New Reservation</span></Link>, '2.2'),
    ]),
    getItem(<Link href="/accounts/profile" ><UserOutlined /><span>Profile</span></Link>, '3'),
    getItem(<div onClick={onClickLogout}><LogoutOutlined /><span>Logout</span></div>, '4'),
  ];

  return (
    <Layout className="min-h-screen"
    >
      <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
        <Menu theme="dark" items={items} defaultSelectedKeys={[selectedKey]} mode="inline" className="mt-2"
          onClick={(e) => setSelectedKey(e.key)}
        />

      </Sider>
      <Layout className="site-layout">
        <div
          className="h-12 flex justify-center items-center p-0 bg-secondary"
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
