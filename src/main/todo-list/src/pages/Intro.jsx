import { useEffect, useState } from "react"
import { AnimatePresence, motion } from "framer-motion"
import axios from "axios"

import styles from "../styles/Intro.module.css"

import TodoTable from "../components/TodoTable"
import Spinner from "../components/Spinner"

const Intro = ({setIsLoading}) => {
  const [userName, setUserName] = useState("당신");
  const [logined, setLogined] = useState(false);
  const [setting, setSetting] = useState(false);
  const [mainList, setMainList] = useState(0);
  const [tempMainList, setTempMainList] = useState(0);
  const [loading, setLoading] = useState(false);
  const [todoLists, setTodoLists] = useState([]);

  const getMainInfo = async() => {
    if(sessionStorage.getItem("accessToken") != null) {
      const res = await axios.get("/api/v1/user/main", 
        {headers:{Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`}})

      if(res.data.resultType === "S") {
        setUserName(res.data.body.userName);
        setMainList(res.data.body.mainToDoListId);
        setTempMainList(res.data.body.mainToDoListId);
        setLogined(true);
      } else if(res.data.resultType === "F") {
        console.log(res.data.error)
      }
    }
  }

  const getTodoLists = async() => {
    if (sessionStorage.getItem("accessToken") != null) {
      const res = await axios.get("/api/v1/list/lists", {
        headers: {
          Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`
        }
      });
      if(res.data.resultType === "S"){
        setTodoLists(res.data.body.toDoListDto)
      } else if(res.data.resultType === "F") {
        console.log(res.data.error)
      }
      
    }
  }

  const changeMainList = async() => {
    setLoading(true)
    const res = await axios.put("/api/v1/user/main",
      {mainToDoListId:tempMainList},
      {headers:{Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`}}
    )
    if(res.data.resultType === "S"){
      setMainList(res.data.body.toDoListDto)
      setTempMainList(res.data.body.toDoListDto)
      
      setSetting(false)
    } else if(res.data.resultType === "F") {
      console.log(res.data.error)
    }
    setLoading(false)
  }

  useEffect(() => {
    getMainInfo()
    getTodoLists()
  }, [])

  return (
    <div className={styles.intro}>
      <h1><span className={styles.emph}>{userName}</span>의 하루</h1>
      <AnimatePresence>
      {logined?
        <motion.div
          className={styles.mainList}
          initial={{scale: 0}}
          animate={{scale: 1}}
          transition={{
            type: "spring", 
            stiffness: 100, 
            damping: 20
          }}
        >
          <div className={styles["mainList-conf-con"]}>
            <div>
              <motion.span
                className={`material-symbols-outlined ${styles.pointer}`}
                whileHover={{
                  scale: 1.3,
                  color: "rgb(10, 10, 10)",
                  rotate: "60deg"
                }}
                onClick={()=>setSetting(pre=>!pre)}
              >
                Settings
              </motion.span>
              <AnimatePresence>
              {setting && 
                <motion.div
                  className={styles["mainList-conf"]}
                  initial={{scaleY: 0}}
                  animate={{scaleY: 1}}
                  exit={{ scaleY: 0}}
                >
                  <div
                    className={styles["mainList-conf-list"]}
                  >
                    {todoLists.map((todoList, idx)=> {
                      return(
                        <motion.div 
                          key={idx} 
                          className={`${styles.pointer}`}
                          style={{
                            backgroundColor:tempMainList===todoList.listId ?
  "rgba(0, 0, 0, 0.3)":"rgba(0, 0, 0, 0)",
                          }}
                          onClick={()=>{
                            setTempMainList(todoList.listId)
                          }}
                        >
                          {todoList.listName}
                        </motion.div>
                      );
                    })}
                  </div>
                  {!loading?
                  <motion.div
                    className={`material-symbols-outlined ${styles.pointer}`}
                    initial={{
                      color: "rgb(0, 0, 0)",
                      textAlign: "center"
                    }}
                    whileHover={{
                      color:"rgb(50, 150, 50)",
                      scale: 1.3
                    }}
                    onClick={changeMainList}
                  >
                    check
                  </motion.div>:
                  <Spinner />
                  }
                  
                </motion.div>
              }
              </AnimatePresence>
            </div>
          </div>
          <div className={styles["mainList-list"]}>
          {mainList!==null?
            <TodoTable 
              listId={mainList}
              setIsLoading={setIsLoading}
            />:
            <div
              className={styles["mainList-null"]}
            >메인 화면에 띄울 Todo List를 선택 해 주세요.↗️</div>
          }
          </div>
        </motion.div>:
        <div
          className={styles["intro-con"]}
        >
          <div
            className={styles["intro-text1"]}
          >
          <img 
            src="/img/1.png" 
            alt="" 
            className={styles["intro-img"]}
          />
          </div>
          <div
            className={styles["intro-text2"]}
          >
            <br /><br /><br />
            <span>
              사용자 맞춤형 
              할 일 관리 서비스입니다.
            </span><br /><br /><br /><br /><br /><br />
            <span>
              당신이 등록한 많은 할 일 중에   
            </span><br />
            <span>
            <span className={styles.emph}>AI</span>가 추천해주는 일을 먼저 해보세요!
            </span>
          </div>
        </div>
      }
      </AnimatePresence>
      
    </div>
  );
}

export default Intro;