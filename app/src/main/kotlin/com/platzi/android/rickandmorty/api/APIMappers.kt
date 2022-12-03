package com.platzi.android.rickandmorty.api

import com.platzi.android.rickandmorty.database.CharacterEntity
import com.platzi.android.rickandmorty.database.LocationEntity
import com.platzi.android.rickandmorty.database.OriginEntity
import com.platzi.android.rickandmorty.domain.Character
import com.platzi.android.rickandmorty.domain.Location
import com.platzi.android.rickandmorty.domain.Origin

fun CharacterResponseServer.toCharacterDomainList(): List<Character> = results.map {
    it.run{
        Character(
            id,
            name,
            image,
            gender,
            species,
            status,
            origin.toOriginDomain(),// Estas son llamadas funciones de extension
            location.toLocationDomain(),
            episodeList.map { episode -> "$episode/" }
        )
    }
}

/*fun CharacterServer.toCharacterEntity() = CharacterEntity(
    id,
    name,
    image,
    gender,
    species,
    status,
    origin.toOriginEntity(),
    location.toLocationEntity(),
    episodeList
)  //comentado en clase 13*/

fun OriginServer.toOriginDomain() = Origin(
    name,
    url
)

fun LocationServer.toLocationDomain() = Location(
    name,
    url
)
