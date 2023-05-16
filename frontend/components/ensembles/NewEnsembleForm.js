import {Button, Form, Input} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {createFunction} from '../../lib/FunctionService';
import {listMetrics} from '../../lib/MetricService';
import {CloseSquareOutlined, PlusSquareOutlined} from '@ant-design/icons';
import SLOSelection from '../misc/SLOSelection';


const NewEnsembleForm = ({setNewEnsemble}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [metrics, setMetrics] = useState([]);
  const [slos, setSlos] = useState([]);
  const [sloId, setSloId] = useState(0);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listMetrics(token, setMetrics, setError);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createFunction(values.runtime, values.name, values.code, token, setNewEnsemble, setError);
      console.log(values);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onClickAddSLO = () => {
    setSlos((prevSlos) => [...prevSlos, {
      id: sloId,
      name: '',
      metricType: 'number',
      expression: '',
      value: [],
    }]);
    setSloId((prevId) => prevId + 1);
  };

  const onClickRemoveSLO = (id) => {
    console.log(id);
    setSlos((prevSlos) => prevSlos.filter((slo) => slo.id !== id));
  };

  useEffect(() => {
    console.log(slos);
  }, [slos]);

  const onChangeMetricSelect = (value, sloId) => {
    const metric = metrics.find((metric) => metric.metric_id === value);
    console.log(metric);
    setSlos((prevSlos) => {
      return prevSlos.map((slo) => {
        if (slo.id === sloId) {
          slo.name = metric?.metric;
          slo.metricType = metric?.metric_type?.type;
        }
        return slo;
      });
    });
  };

  const onChangeExpressionSelect = (value, sloId) => {
    setSlos((prevSlos) => {
      return prevSlos.map((slo) => {
        if (slo.id === sloId) {
          slo.expression = value;
        }
        return slo;
      });
    });
  };


  return (
    <>
      <div>
        {slos.map((slo) => {
          return (
            <div key={slo.id}>
              <SLOSelection
                metrics={metrics}
                selectedMetrics={slos.map((metricSlo) => metricSlo.name)}
                slo={slo}
                updateMetric={onChangeMetricSelect}
                updateExpression={onChangeExpressionSelect}
              />
              <Button type="ghost" icon={<CloseSquareOutlined />} onClick={() => onClickRemoveSLO(slo.id)}/>
            </div>
          );
        })}
        <Button className="mt-8" type="primary" icon={<PlusSquareOutlined />} onClick={onClickAddSLO}>SLO</Button>

      </div>

      <Form
        name="newFunctionForm"
        form={form}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
        layout="vertical"
      >
        <Form.Item
          label="Name"
          name="name"
          rules={[
            {
              required: true,
              message: 'Please input a name!',
            },
          ]}
        >
          <Input className="w-40" />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit">
            Create
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewEnsembleForm.propTypes = {
  setNewEnsemble: PropTypes.func.isRequired,
};

export default NewEnsembleForm;
