import React from 'react';
import { ReactComponent as HeartActivate } from '../assets/Icons/heart/activate.svg';
import { ReactComponent as HeartDefault } from '../assets/Icons/heart/default.svg';
import { ReactComponent as BellOff } from '../assets/Icons/bell/off.svg';
import { ReactComponent as BellOn } from '../assets/Icons/bell/on.svg';
import { ReactComponent as Calendar } from '../assets/Icons/calendar.svg';
import { ReactComponent as Check } from '../assets/Icons/check.svg';
import { ReactComponent as CheckBoxNone } from '../assets/Icons/check_box/noneclick.svg';
import { ReactComponent as CheckBoxClick } from '../assets/Icons/check_box/onclick.svg';
import { ReactComponent as ChevronDown } from '../assets/Icons/chevron_down.svg';
import { ReactComponent as ChevronLeft } from '../assets/Icons/chevron_left.svg';
import { ReactComponent as ChevronRight } from '../assets/Icons/chevron_right.svg';
import { ReactComponent as ChevronUp } from '../assets/Icons/chevron_up.svg';
import { ReactComponent as Crown } from '../assets/Icons/crown.svg';
import { ReactComponent as Delete } from '../assets/Icons/delete.svg';
import { ReactComponent as DeleteSmall } from '../assets/Icons/delete_small.svg';
import { ReactComponent as Dots } from '../assets/Icons/dots.svg';
import { ReactComponent as Edit } from '../assets/Icons/edit.svg';
import { ReactComponent as Eye } from '../assets/Icons/eye.svg';
import { ReactComponent as Hamburger } from '../assets/Icons/hamburger.svg';
import { ReactComponent as Logout } from '../assets/Icons/logout.svg';
import { ReactComponent as Mail } from '../assets/Icons/mail.svg';
import { ReactComponent as People } from '../assets/Icons/people.svg';
import { ReactComponent as PlusM } from '../assets/Icons/plus_m.svg';
import { ReactComponent as PlusS } from '../assets/Icons/plus_s.svg';
import { ReactComponent as Remove } from '../assets/Icons/remove.svg';
import { ReactComponent as Send } from '../assets/Icons/send.svg';
import { ReactComponent as SwapVert } from '../assets/Icons/swap_vert.svg';
import { ReactComponent as UnfoldMore } from '../assets/Icons/unfold_more.svg';

import { ReactComponent as Bear } from '../assets/Icons/animals/Bear.svg';
import { ReactComponent as DogFace } from '../assets/Icons/animals/Dog Face.svg';
import { ReactComponent as Fish } from '../assets/Icons/animals/Fish.svg';
import { ReactComponent as Fox } from '../assets/Icons/animals/Fox.svg';
import { ReactComponent as Frog } from '../assets/Icons/animals/Frog.svg';
import { ReactComponent as Hamster } from '../assets/Icons/animals/Hamster.svg';
import { ReactComponent as Koala } from '../assets/Icons/animals/Koala.svg';
import { ReactComponent as LadyBeetle } from '../assets/Icons/animals/Lady Beetle.svg';
import { ReactComponent as Lion } from '../assets/Icons/animals/Lion.svg';
import { ReactComponent as MonkeyFace } from '../assets/Icons/animals/Monkey Face.svg';
import { ReactComponent as MouseFace } from '../assets/Icons/animals/Mouse Face.svg';
import { ReactComponent as Octopus } from '../assets/Icons/animals/Octopus.svg';
import { ReactComponent as Orangutan } from '../assets/Icons/animals/Orangutan.svg';
import { ReactComponent as Panda } from '../assets/Icons/animals/Panda.svg';
import { ReactComponent as Parrot } from '../assets/Icons/animals/Parrot.svg';
import { ReactComponent as Penguin } from '../assets/Icons/animals/Penguin.svg';
import { ReactComponent as PigFace } from '../assets/Icons/animals/Pig Face.svg';
import { ReactComponent as PolarBear } from '../assets/Icons/animals/Polar Bear.svg';
import { ReactComponent as RabbitFace } from '../assets/Icons/animals/Rabbit Face.svg';
import { ReactComponent as Rooster } from '../assets/Icons/animals/Rooster.svg';
import { ReactComponent as Shark } from '../assets/Icons/animals/Shark.svg';
import { ReactComponent as Skunk } from '../assets/Icons/animals/Skunk.svg';
import { ReactComponent as SpoutingWhale } from '../assets/Icons/animals/Spouting Whale.svg';
import { ReactComponent as Swan } from '../assets/Icons/animals/Swan.svg';
import { ReactComponent as Turtle } from '../assets/Icons/animals/Turtle.svg';
import { ReactComponent as Whale } from '../assets/Icons/animals/Whale.svg';
import { ReactComponent as Wolf } from '../assets/Icons/animals/Wolf.svg';

const icons = {
  heartFill: HeartActivate,
  heartDefault: HeartDefault,
  bellOff: BellOff,
  bellOn: BellOn,
  calendar: Calendar,
  check: Check,
  checkBoxNone: CheckBoxNone,
  checkBoxClick: CheckBoxClick,
  chevronDown: ChevronDown,
  chevronLeft: ChevronLeft,
  chevronRight: ChevronRight,
  chevronUp: ChevronUp,
  crown: Crown,
  delete: Delete,
  deleteSmall: DeleteSmall,
  dots: Dots,
  edit: Edit,
  eye: Eye,
  hamburger: Hamburger,
  logout: Logout,
  mail: Mail,
  people: People,
  plusM: PlusM,
  plusS: PlusS,
  remove: Remove,
  send: Send,
  swapVert: SwapVert,
  unfoldMore: UnfoldMore,

  '@animals/bear': Bear,
  '@animals/dog_face': DogFace,
  '@animals/fish': Fish,
  '@animals/fox': Fox,
  '@animals/frog': Frog,
  '@animals/hamster': Hamster,
  '@animals/koala': Koala,
  '@animals/lady_beetle': LadyBeetle,
  '@animals/lion': Lion,
  '@animals/monkey_face': MonkeyFace,
  '@animals/mouse_face': MouseFace,
  '@animals/octopus': Octopus,
  '@animals/orangutan': Orangutan,
  '@animals/panda': Panda,
  '@animals/parrot': Parrot,
  '@animals/penguin': Penguin,
  '@animals/pig_face': PigFace,
  '@animals/polar_bear': PolarBear,
  '@animals/rabbit_face': RabbitFace,
  '@animals/rooster': Rooster,
  '@animals/shark': Shark,
  '@animals/skunk': Skunk,
  '@animals/spouting_whale': SpoutingWhale,
  '@animals/swan': Swan,
  '@animals/turtle': Turtle,
  '@animals/whale': Whale,
  '@animals/wolf': Wolf,
};

const Icon = ({ name, className, color }) => {
  const IconComponent = icons[name];
  if (!IconComponent) {
    console.error(`Icon "${name}" does not exist.`);
    return null;
  }

  const style =
    name === 'dots' ? { fill: color }
    : name === 'chevronLeft' ? { stroke: color }
    : { stroke: color, fill: color };

  return <IconComponent className={className} style={style} />;
};

export default Icon;
