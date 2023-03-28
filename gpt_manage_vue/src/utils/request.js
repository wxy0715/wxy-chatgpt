import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 2400000
})
export default service