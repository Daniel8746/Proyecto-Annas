import requests
from bs4 import BeautifulSoup
from modelos.libro import Libro

link = "https://es.annas-archive.org/"

def buscar_libro(nombre):
	resultados = []
	r = requests.get(link + "search?q=" + nombre)
	soup = BeautifulSoup(r.text, "html.parser")

	for item in soup.find_all("div", class_="flex pt-3 pb-3 border-b"):
		# portada
		img_tag = item.find("img")
		portada = img_tag["src"] if img_tag else None
		
		# título (primer <a> con clase text-lg)
		titulo_tag = item.find("a", class_="text-lg")
		titulo = titulo_tag.get_text(strip=True) if titulo_tag else None
		enlace = titulo_tag["href"] if titulo_tag else None

        # autor (segundo <a>, con clase text-sm)
		autor_tag = item.find("a", class_="text-sm")
		autor = autor_tag.get_text(strip=True) if autor_tag else None

		resultados.append(
			Libro(enlace = enlace, titulo = titulo, autor = autor, portada = portada)
		)       

	return resultados

def servidor_descarga(enlace):
	link_servidor = []
	descripcion = ""
	r = requests.get(link + enlace)
	soup = BeautifulSoup(r.text, "html.parser")
	
	descripcion = soup.find("div", class_ = "mt-4 js-md5-top-box-description").find("div", class_ = "mb-1").get_text(strip=True)

	ul = soup.find_all("ul", class_="list-inside mb-4 ml-1")[1]
	
	for li in ul.find_all("li"):
		link_servidor.append(li.find("a")["href"][1:])
	
	return descripcion, link_servidor

def descargar_libro(link_servidor):
    # Solo obtenemos el link final del servidor, no hacemos download en Python
    r = requests.get(link + link_servidor)
    soup = BeautifulSoup(r.text, "html.parser")
    link_descarga = soup.find("p", class_="font-bold a")["href"]
    return link_descarga  # lo pasas a Android/WebView