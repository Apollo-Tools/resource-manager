import {python} from '@codemirror/lang-python';

export function getEditorExtension(runtimeName) {
  if (runtimeName.toLowerCase().startsWith('python')) {
    return python();
  }
  return null;
}
