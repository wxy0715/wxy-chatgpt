import request from '@/utils/request.js'

function saveQuestion(data){
  return request.post('/api/saveQuestion',data)
}
function question(msg){
  return request.get('/api/question?question='+msg)
}
export default{
  saveQuestion,
  question
}



