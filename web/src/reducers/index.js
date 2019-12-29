import { combineReducers } from 'redux';
import files from './filesReducer';
import projects from './projectReducer';
import file from './fileReducer';
import branches from './branchesReducer';
import jobs from "./jobsReducer";
import users from './usersReducer';

const rootReducer = combineReducers({
  branches,
  files,
  file,
  jobs,
  projects,
  users,
});

export default rootReducer;
