import request from '@/utils/request.js'

function saveQuestion(data){
  return request.post('/api/saveQuestion',data)
}
function question(data){
  return request.post('/api/question',data)
}
export default{
  saveQuestion,
  question
}



