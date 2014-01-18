<?php
include 'mysql/lib.php';

class ApiConnection{
	protected $outMsg="";
	protected $conector ;
	protected $generalBD="agosal2";
	protected $prefix_UserDB = "agosal_user_";

	public function __construct(){
		$this->conector = new mysqlConnection();
		$this->generalBD ="agosal2";
	}
/*
	Atiende la solicitud del cliente y la ejecuta, analizando previamente su contenido.
*/
	public function respondRequest(){
	
// 	echo '{"errorCode":"234"{"Tipo":"aaaaamigoooo","array":[{"1":"uno","2":"d\"o\"s"},{"d\"o\"s":2,"uno":1,"tres":3}]}} ';
		if (!isset($_POST["type"])){
			$this->protocolError();
			echo $this->outMsg;
			return;
		}
		switch($_POST["type"]){
			case "Select":
				if ($this->connect()){
					$this->launchQuery();
					$this->desconnect();
				}
				else{
					invalidAccess();
				}
			break;
			case "SP":
				if ($this->connect()){
					$this->launchSP();
					$this->desconnect();
				}
				else{
					$this->invalidAccess();
				}
			break;
			case "CheckCredentials":
				$_POST["bd"]= $this->generalBD;
				if ($this->connect()){
					$nRow = $this->conector->query("Select SystemID from Users where userID = '".$_POST["user"]."'");
					if ($nRow < 0){
						$this->errorHandle($this->conector->getErrorCode());
					}
					else{
						$value = $this->conector->getStatment();
						$SystemID = $value[0]["SystemID"];
						if (isset($SystemID)){
							$this->outMsg = $this->buildMessage(0,array("userDB"=>$this->prefix_UserDB.$SystemID) ) ;
						}
						else{
							$this->accessNotAllowed();
						}
					}
					$this->desconnect();

					/*
if ($this->connect()){
					$_POST["bd"]="agosal";
					$this->outMsg = $this->buildMessage(0,array());
					$this->desconnect();
				}
				else{
					$this->invalidAccess();
				}

					*/
				}
				else{
					$this->invalidAccess();
				}
			break;
			default:
				$this->invalidAction();
			break;
		}
// 		echo '{"errorCode":"234"{"Tipo":"aaaaamigoooo","array":[{"1":"uno","2":"d\"o\"s"},{"d\"o\"s":2,"uno":1,"tres":3}]}} ';
		echo $this->outMsg;
	}
	

	/*
		$value = array(1,2,3,4,5,6);
		$value["Tipo"] = "aaaaamigoooo";
// 		$value["array"] = array(array("1"=>"uno","2"=>"dos"),array("dos"=>2, "uno"=>1,"tres"=>3));
		$value["array"] = array(array("1"=>"uno","2"=>"d\"o\"s"),array("d\"o\"s"=>2, "uno"=>1,"tres"=>3));
		$msg = $this->buildMessage(234,$value);
		echo $msg ."\n";
		$obj = json_decode($msg,true);

		print_r($obj);
		if(isset($obj['errorCode'])) 	echo $obj['errorCode']." \n";
		else 	echo " No  Existeee \n";


		foreach($obj as $key => $value){
			echo $key;
		}
	*/

// 	{"errorCode":"234"{"Tipo":"aaaaamigoooo","array":[{"1":"uno","2":"d\"o\"s"},{"d\"o\"s":2,"uno":1,"tres":3}]}} 

	protected function accessNotAllowed(){
		$this->errorHandle(12);
	}

	protected function protocolError(){
		$this->errorHandle(10);
	}

	protected function invalidAction(){
		$this->errorHandle(11);

	}

	protected function invalidAccess(){
		$this->errorHandle($this->conector->getErrorCode());
	}


	protected function errorHandle($type){
		$atackMsg = " Este hecho se considera un posible ataque al sistema, por lo que podría denegarse su acceso.";
		$infoMsg = "Por favor, inténtelo más tarde. Si vuelve a ocurrir no dude en comunicarselo al administrador del servicio";
		switch($type){
			case 10: // Falta de parámetros obligatorios
				$errorCode=10;
				$msg= "Se ha detectado la falta de parámetros obligatorios.\n".$atackMsg;
			break;
			case 11: // Servicio inexistente
				$errorCode=11;
				$msg= "Se ha seleccionado un servicio no soportado.\n".$atackMsg;
			break;
			case 12: // Servicio inexistente
				$errorCode=11;
				$msg= "El usuario solicitado no posee acceso al sistema.\n";
			break;
			case 31: // Error en el uso SSL
				$errorCode=31;
				$msg= "Se ha producido un error en la comunicación SSL.\n".$atackMsg;
			break;
			case 32: // Error en el autocommit.
				$errorCode=32;
				$msg= "Se ha producido un error en la comunicación.\n".$atackMsg;
			break;
			case 420: // Inyección SQL
				$errorCode=420;
				$msg= "Se ha detectado inyección SQL.\n".$atackMsg;
			break;
			case 430: // Error en formato de parámetros (SP)
				$errorCode=430;
				$msg= "Error en el formato de los parámetros.\n ";//\"".$this->conector->getErrorMsg(). "\"";
			break;
			case 1045: // Error de contraseña
				$errorCode=20;
				$msg= "Usuario o contraseña incorrectos";
			break;
			case 1046: // BD no seleccionada
				$errorCode=414;
				$msg= "BD no seleccionada .\n \"".$this->conector->getErrorMsg(). "\"";
			break;
			case 1146: // Tabla desconocida
				$errorCode=410;
				$msg= "Tabla desconocida.\n \"".$this->conector->getErrorMsg(). "\"";
			break;
			case 1049: // BD desconocida
				$errorCode=411;
				$msg= "BD desconocida.\n \"".$this->conector->getErrorMsg(). "\"";
			break;
			case 1054: // Campo desconocido
				$errorCode=412;
				$msg= "Campo desconocido.\n \"".$this->conector->getErrorMsg(). "\"";
			break;
			case 1064: // Expresion incorrecta
				$errorCode=413;
				$msg= "Expresión desconocida.\n \"".$this->conector->getErrorMsg(). "\"";
			break;
			case 1305: // Sp no existente
				$errorCode=431;
				$msg= "SP desconocido.\n \"".$this->conector->getErrorMsg(). "\"";
			break;
			case 1318: // nº de parámetros incorrecto. SP
				$errorCode=432;
				$msg= "Número de parámetros erróneo.\n \"".$this->conector->getErrorMsg(). "\"";
			break;
			case 2003: // Error conexión con el servidor
				$errorCode=30;
				$msg= "Error de conexión con el servidor.";
			break;
			case 2005: // servidor desconocido
				$errorCode=33;
				$msg= "Error, servidor desconocido.";
			break;
			default: // Error desconocido.
				$errorCode=999;
				$msg= "Error: $type desconocido.Si vuelve a ocurrir no dude en comunicarselo al administrador del servicio.\n \"".$this->conector->getErrorMsg(). "\"";
			
		}
		$value["msg"] = $msg;
		$this->outMsg = $this->buildMessage(-$errorCode,$value);
}


	protected function sqlInjectionAnalysis(){
// mysqli_real_escape_string
	}

	protected function connect(){
		return $this->conector->connect("localhost", $_POST["bd"], $_POST["user"], $_POST["pass"]);
	}

	protected function desconnect(){
		$this->conector->desconnect();
	}

	protected function launchQuery(){
		$nRow = $this->conector->query($_POST["clause"]);
		if ($nRow < 0){
			$this->errorHandle($this->conector->getErrorCode());
		}
		else{
			$value["content"] =  $this->conector->getStatment();
			$this->outMsg = $this->buildMessage($nRow,$value);
		}
	}

	protected function launchSP(){
		$code = $this->conector->call($_POST["SP"],$_POST["args"]);
		if ($code < 0){
			$this->errorHandle($this->conector->getErrorCode());
		}
		else{
			if ($code == 0){
				$this->conector->commit();
			}
			else{
				$this->conector->rollback();
			}
			$value =  $this->conector->getStatment();
			$this->outMsg = $this->buildMessage($code,$value);
		}

	}

	/* El parámetro content presenta la información a trasmitir al cliente. En el caso de una query válida contendrá todos los registros con sus diversos campos.
	En el de un procedure indicará el mensaje e/o información deseado. En caso de error no mandará nada si es un error del sistema(SQL) o el mensaje indicado si es un error capturado.*/
	protected function buildMessage($errorCode,$content){
		$content["errorCode"] = $errorCode;
		$msg = json_encode($content);
		return $msg;
	}

	public function prueba(){
		echo "Tolaa\n";
	}

};
	$obj = new ApiConnection();
	$obj->respondRequest();
?>
