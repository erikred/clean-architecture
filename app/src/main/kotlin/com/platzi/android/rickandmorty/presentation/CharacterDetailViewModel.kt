package com.platzi.android.rickandmorty.presentation


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.platzi.android.rickandmorty.api.EpisodeServer
import com.platzi.android.rickandmorty.domain.Character
import com.platzi.android.rickandmorty.domain.Episode
import com.platzi.android.rickandmorty.usecases.GetEpisodeFromCharacterUseCase
import com.platzi.android.rickandmorty.usecases.GetFavoriteCharacterStatusUseCase
import com.platzi.android.rickandmorty.usecases.UpdateFavoriteCharacterStatusUseCase
import io.reactivex.disposables.CompositeDisposable

class CharacterDetailViewModel(private  val character: Character? = null,
                               private val getFavoriteCharacterStatusUseCase: GetFavoriteCharacterStatusUseCase,
                               private val updateFavoriteCharacterStatusUseCase: UpdateFavoriteCharacterStatusUseCase,
                               private val getEpisodeFromCharacterUseCase: GetEpisodeFromCharacterUseCase): ViewModel() {

    //region Fields

    private val disposable = CompositeDisposable()

    private val _characterValues = MutableLiveData<Character>()
    val characterValues: LiveData<Character> get() = _characterValues

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> get() = _isFavorite

    private val _events = MutableLiveData<Event<CharacterDetailNavigation>>()
    val events: LiveData<Event<CharacterDetailNavigation>> get() = _events
    //endRegion

    //region Override Methods & Callbacks
    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
    //endRegion

    //region Public Methods
    fun onCharacterValidation(){
        if(character==null){
            _events.value = Event(CharacterDetailNavigation.CloseActivity)
            return
        }

        _characterValues.value = character
        validateFavoriteCharacterStatus(character.id)
        requestShowEpisodeList(character.episodeList)
    }

    fun onUpdateFavoriteCharacterStatus(){
        disposable.add(

                updateFavoriteCharacterStatusUseCase.invoke(character!!).subscribe {// No es considerada una buena practica poner el doble signo de admiracion en lugar de ello se sugiere unar el operador let
                    isFavorite ->
                    _isFavorite.value = isFavorite
                }

        )
    }

    //endRegion
    //region Private Methods
    private fun validateFavoriteCharacterStatus(characterId: Int){
        disposable.add(

                getFavoriteCharacterStatusUseCase.invoke(characterId).subscribe{ isFavorite ->
                    _isFavorite.value = isFavorite
                }
        )
    }

    private fun requestShowEpisodeList(episodeUrlList: List<String>){
        disposable.add(
            getEpisodeFromCharacterUseCase.invoke(episodeUrlList)
                .doOnSubscribe {
                    _events.value = Event(CharacterDetailNavigation.ShowEpisodeListLoading)
                }
                .subscribe(
                    {
                        episodeList ->
                        _events.value = Event(CharacterDetailNavigation.HideEpisodeListLoading)
                        _events.value = Event(CharacterDetailNavigation.ShowEpisodeList(episodeList))
                    },
                    {
                        error ->
                        _events.value = Event(CharacterDetailNavigation.HideEpisodeListLoading)
                        _events.value = Event(CharacterDetailNavigation.ShowEpisodeError(error))
                    }
                )
        )
    }

    //endRegion

    //region Inner Classes & Interfaces
    sealed class CharacterDetailNavigation{
        data class ShowEpisodeError(val error: Throwable): CharacterDetailNavigation()
        data class ShowEpisodeList(val episodeList: List<Episode>): CharacterDetailNavigation()
        object CloseActivity: CharacterDetailNavigation()
        object HideEpisodeListLoading: CharacterDetailNavigation()
        object ShowEpisodeListLoading: CharacterDetailNavigation()
    }
    //endRegion
}