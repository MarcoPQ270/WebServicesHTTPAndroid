package com.example.mywebservices

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {

    var wsConsultar : String="http://192.168.1.68/WEBSERVICES/mostrarAlumno.php"
    var wsInsertar: String="http://http://192.168.1.68/WEBSERVICES/InsertarAlumno.php"
    var hilo: ObtenerServicioWeb? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun ConsultaXNoControl(v:View){
        if(etNoControl.text.isEmpty()){
            Toast.makeText(this, "Ingrese un numero de control", Toast.LENGTH_SHORT).show();
            etNoControl.requestFocus()
        }else{
            val no = etNoControl.text.toString()
            hilo=ObtenerServicioWeb()
            hilo?.execute("Consulta",no,"","","")
        }
    }

    fun InsertaAlumno(v:View){
        if(etNoControl.text.isEmpty() ||etCarrera.text.isEmpty() || etNombre.text.isEmpty() || etTelefono.text.isEmpty() ){
            Toast.makeText(this, "Hay campos vacios", Toast.LENGTH_SHORT).show();
            etNoControl.requestFocus()
        }else{
            val no =etNoControl.text.toString()
            val carr= etCarrera.text.toString()
            val nom = etNombre.text.toString()
            val tel= etTelefono.text.toString()
            hilo=ObtenerServicioWeb()
            hilo?.execute("Insertar",no,carr,nom,tel)
        }
    }


    inner class ObtenerServicioWeb():AsyncTask<String, String, String>(){
        override fun doInBackground(vararg params: String?): String {
            var Url : URL? = null
            var sResultado = ""
            try  {
                val urlConn : HttpURLConnection
                val prinout : DataOutputStream
                val input : DataInputStream
                if(params[2].toString().isEmpty() && params[3].toString().isEmpty()){
                    Url=URL(wsConsultar)
                }else{
                    Url=URL(wsInsertar)
                }
                urlConn=Url.openConnection() as HttpURLConnection
                urlConn.doInput=true
                urlConn.doOutput=true
                urlConn.useCaches=false
                urlConn.setRequestProperty("Content-Type","application/json")
                urlConn.setRequestProperty("Accept","application/json")
                urlConn.connect()

                //Codigo para preparar los datos a enviar al web service
                val jsonParam = JSONObject()
                jsonParam.put("nocontrol",params[1])
                jsonParam.put("carrera",params[2])
                jsonParam.put("nombre",params[3])
                jsonParam.put("telefono",params[4])

                val oS = urlConn.outputStream
                val writer=BufferedWriter(OutputStreamWriter(oS, "UTF-8"))
                writer.write(jsonParam.toString())
                writer.flush()
                writer.close()

                val respuesta = urlConn.responseCode
                val result = StringBuilder()
                if(respuesta==HttpURLConnection.HTTP_OK){
                    val inString: InputStream= urlConn.inputStream
                    val isReader= InputStreamReader(inString)
                    val bReader=BufferedReader(isReader)
                    var tempStr: String?
                    while (true){
                        tempStr=bReader.readLine()
                        if(tempStr==null){
                            break
                        }
                            result.append(tempStr)
                    }
                    urlConn.disconnect()
                    sResultado=result.toString()
                }
            }catch (e: MalformedURLException){
                Log.d("Valio Madre",e.message)
            }catch (e:IOException){
                Log.d("Valio Madre",e.message)
            }catch (e:JSONException){
                Log.d("Valio Madre",e.message)
            }catch (e:Exception){
                Log.d("Valio Madre",e.message)
            }

           return sResultado
            
        }//Fin doInBackground    //Metodos que se van a utilizar

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var no: String=""
            var nom: String=""
            var carr: String=""
            var tel: String=""

            //parsear JSon consiste en leer el json para poder sacar los datos
            try {
                val respuestaJSON= JSONObject(result)
                val resultJSON = respuestaJSON.getString("success")
                when{
                    resultJSON=="200"->{
                        val alumnoJSON = respuestaJSON.getJSONArray("alumno")
                        if(alumnoJSON.length() >= 1){
                            no=alumnoJSON.getJSONObject(0).getString("nocontrol")
                            nom=alumnoJSON.getJSONObject(0).getString("nombre")
                            carr=alumnoJSON.getJSONObject(0).getString("carrera")
                            tel=alumnoJSON.getJSONObject(0).getString("telefono")

                            etNoControl.setText(no)
                            etCarrera.setText(carr)
                            etNombre.setText(nom)
                            etTelefono.setText(tel)

                        }//fin if
                    }// fin desicion when

                    resultJSON=="201"->{
                        Toast.makeText(baseContext, "Alumno almacenado", Toast.LENGTH_SHORT).show();
                        etNoControl.setText("")
                        etCarrera.setText("")
                        etNombre.setText("")
                        etTelefono.setText("")
                        etNoControl.requestFocus()
                    }
                    resultJSON=="204"->{
                        Toast.makeText(baseContext, "Alumno no encontrado", Toast.LENGTH_SHORT).show();
                    }
                    resultJSON=="409"->{
                        Toast.makeText(baseContext, "El numero de control ya esta registrado", Toast.LENGTH_SHORT).show();
                    }
                }


            }catch (e:java.lang.Exception){
                Log.d("Valio Madre",e.message)
            }
        }//fin  onPostExecute //Metodos que se van a utilizar

    }// Fin  ObtenerUnServicioWeb

}//Fin De La clase MainActivity
