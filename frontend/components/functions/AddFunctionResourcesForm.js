import {Button, Form, Typography} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {listResources} from '../../lib/ResourceService';
import PropTypes from 'prop-types';
import {addFunctionResources} from '../../lib/FunctionResourceService';
import ResourceTable from '../resources/ResourceTable';

const AddFunctionResourcesForm = ({
  func,
  excludeResourceIds,
  setFinished,
  isSkipable,
}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [resources, setResources] = useState([]);
  const [selectedResourceIds, setSelectedResourceIds] = useState([]);
  const [error, setError] = useState(false);
  Form.useWatch('basic', form);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResources(false, token, setResources, setError)
          .then(() => {
            setResources((prevResources) => {
              let filteredResources = prevResources;
              if (excludeResourceIds != null) {
                filteredResources = prevResources
                    .filter((resource) => !excludeResourceIds.includes(resource.resource_id));
              }
              console.log(filteredResources);
              return filteredResources;
            });
          });
    }
  }, [excludeResourceIds]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('action failed');
      setError(false);
    }
  }, [error]);

  const onClickAdd = async () => {
    if (!checkTokenExpired()) {
      const resources = selectedResourceIds.map((resourceId) => {
        return {resource_id: resourceId};
      });
      await addFunctionResources(func.function_id, resources, token, setError)
          .then(() => setFinished(true));
    }
  };

  const rowSelection = {
    selectedResourceIds,
    onChange: (newSelectedResourceIds) => {
      setSelectedResourceIds(newSelectedResourceIds);
    },
  };

  const onClickSkip = () => {
    setFinished(true);
  };

  if (resources.length === 0) {
    return (<></>);
  }

  return (
    <>
      <Typography.Title level={3}>Add more Resources</Typography.Title>
      <ResourceTable resources={resources} hasActions rowSelection={rowSelection}/>
      <div className="flex">
        <Button disabled={selectedResourceIds.length <= 0 } type="primary" onClick={onClickAdd}>
          Add Resources
        </Button>
        <div className="flex-1"/>
        {isSkipable &&
          (<div>
            <Button type="default" onClick={onClickSkip}>
              Skip
            </Button>
          </div>)
        }
      </div>
    </>
  );
};

AddFunctionResourcesForm.propTypes = {
  func: PropTypes.object.isRequired,
  excludeResourceIds: PropTypes.arrayOf(PropTypes.number.isRequired),
  setFinished: PropTypes.func.isRequired,
  isSkipable: PropTypes.bool,
};

export default AddFunctionResourcesForm;
