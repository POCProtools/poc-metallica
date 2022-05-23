import { TextFieldEntry, isTextFieldEntryEdited } from '@bpmn-io/properties-panel';
import { useService } from 'bpmn-js-properties-panel';

export default function(element) {
  return [
    {
      id: 'metallicaImplementation',
      element,
      component: Implementation,
      isEdited: isTextFieldEntryEdited
    },
    {
      id: 'metallicaBusinessKey',
      element,
      component: BusinessKey,
      isEdited: isTextFieldEntryEdited
    }
  ];
}

function Implementation(props) {
  const { element, id } = props;

  const modeling = useService('modeling');
  const translate = useService('translate');
  const debounce = useService('debounceInput');

  const getValue = () => {
    return element.businessObject.implementation || '';
  }

  const setValue = value => {
    return modeling.updateProperties(element, {
      implementation: value
    });
  }

  return <TextFieldEntry
    id={ id }
    element={ element }
    description={ translate('Service de lancement') }
    label={ translate('Implementation') }
    getValue={ getValue }
    setValue={ setValue }
    debounce={ debounce }
  />
}

function BusinessKey(props) {
  const { element, id } = props;

  const modeling = useService('modeling');
  const translate = useService('translate');
  const debounce = useService('debounceInput');

  const getValue = () => {
    return element.businessObject.businessKey || '';
  }

  const setValue = value => {
    return modeling.updateProperties(element, {
      businessKey: value
    });
  }

  return <TextFieldEntry
    id={ id }
    element={ element }
    description={ translate('Nom du workflow') }
    label={ translate('BusinessKey') }
    getValue={ getValue }
    setValue={ setValue }
    debounce={ debounce }
  />
}
