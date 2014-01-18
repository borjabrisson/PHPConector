<?php

class mysqlConnection{
	protected $connection=null;
	protected $success;
	protected $result;
	protected $errormsg;
	protected $errorCode;
	protected $queryString;
	protected $resultStatment;
	
	public function __construct(){
		$this->success= false;

	}


	protected function getStatmentSP(){
		unset($this->resultStatment);
		$statmentContainer = $dataRow = array();
		while ($this->connection->more_results()){
			$result = $this->connection->store_result();
			if ( (count($statmentContainer) != 0) and (isset($dataRow["resultId"]) ) ) {
				if (isset($this->resultStatment[$dataRow["resultId"]])){
					array_push($this->resultStatment[$dataRow["resultId"]],$statmentContainer);
				}
				else{
					$this->resultStatment[$dataRow["resultId"]] = $statmentContainer;
				}
			}
			unset($statmentContainer);
			$statmentContainer= array();
			while (( $dataRow = $result->fetch_array(MYSQLI_ASSOC) ) != null){
				$statmentContainer []= $dataRow;
			}
			$dataRow = $statmentContainer[0];
			$result->free();
			$this->connection->next_result();
		}

		return $statmentContainer;
	}

	public function call($procedure, $params){
			$this->queryString = "call $procedure ($params)";
			$this->success = @$this->connection->real_query($this->queryString);
			
			if ($this->success){
				$result = $this->getStatmentSP();
				$result = $result[0];
				if ( (!isset($result["errorCode"])) and (!isset($result["error"])) ){ // No se ha indicado estado de error. ERROR!!!
// 					print_r($result);
					$this->errorHandle("Error en la ejecución: \n".$this->connection->error);
					$this->errormsg = $this->connection->error;
					return -1;
				}
				if (isset($result["errorCode"])) $this->resultStatment["errorCode"] = $result["errorCode"];
				if (isset($result["error"]))$this->resultStatment["errorCode"] = $result["error"];

				$this->success = $this->resultStatment["errorCode"];
				if (isset($result["msg"]) ){
					$this->resultStatment["msg"] = $result["msg"]; // ### Expandir y tratar los tipos xaxis.
				}
// 				print_r($this->resultStatment);
			} else{
				$this->errorHandle("Error en la ejecución: \n".$this->connection->error);
				$this->errormsg = $this->connection->error;
				return -1;
			}
			return $this->success;
	}
	
	public function query($query){
		if ($this->success){
			$this->queryString = $query;
			$this->success = @$this->connection->real_query($this->queryString);
			if ($this->success){
				$result = $this->connection->use_result();
				while (( $dataRow = $result->fetch_array(MYSQLI_ASSOC) ) != null){
					$this->resultStatment[] = $dataRow;
				}
				$result->free();
// 				print_r($this->resultStatment);
				return count($this->resultStatment);
			}
			else {
				$this->errorHandle("Error en la ejecución de query: \n".$this->connection->error);
				$this->errormsg = $this->connection->error;
				return -1;
			}
		}
	}

	public function connect($host, $database, $user, $password){
		$this->connection= mysqli_init();
		if (@$this->connection->real_connect($host, $user, $password, $database)){
			if (!$this->connection->autocommit(false)){
				$this->errormsg= "Error. Can't set mysql autocommit to false";
				$this->errorHandle("Error autocommit: \n".$this->connection->connect_error);
				$this->desconnect();
				return false;
			}
		} else{
			$this->errormsg= $this->connection->connect_error;
			$this->errorHandle("Error conection: \n".$this->connection->connect_error);
			$this->connection=null;
			return false;
		}
		$this->success= true;
		return true;//$this->success;
	}
	
	public function desconnect(){
		if (!is_null($this->connection)){
			$this->connection->close();
			$this->connection=null;
		}
	}

	public function commit(){
		if (!is_null($this->connection)) {
			@$this->connection->commit();
			$this->desconnect();
		}
	}
	
	public function rollback(){
		if (!is_null($this->connection)) {
			@$this->connection->rollback();
			$this->desconnect();
		}
	}

	protected function errorHandle($msg){
		$this->errorCode = $this->connection->errno;
	}

	public function getStatment(){
	
		return $this->resultStatment;
	}

	public function getErrorCode(){
		return $this->errorCode;
	}

	public function getErrorMsg(){
		return $this->errormsg;
	}

};

// $obj = new mysqlConnection();
// 	$obj->connect("localhost","bd","root","b.briss0n89");
// // 	$obj->call("new_note","'2021-04-09','21:31','Prueba de lib','2','null'"); $obj->rollback();
// 
// 	$obj->query("Show tables");
// 
// 	echo $obj->getErrorCode()."\n";
// 	$obj->desconnect();
?>